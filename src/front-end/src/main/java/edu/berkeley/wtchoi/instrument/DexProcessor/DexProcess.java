package edu.berkeley.wtchoi.instrument.DexProcessor;

import edu.berkeley.wtchoi.instrument.util.Debug;
import edu.berkeley.wtchoi.instrument.util.EchoingWriter;
import edu.berkeley.wtchoi.instrument.util.WriterFlushingWrapper;
import edu.berkeley.wtchoi.instrument.ApkInfo;
import edu.berkeley.wtchoi.instrument.DexProcessor.annotatedPrintingVisitor.AnnotatedApplicationPrintingVisitor;
import edu.berkeley.wtchoi.instrument.DexProcessor.il.ApplicationInfo;
import edu.berkeley.wtchoi.instrument.DexProcessor.il.ManifestInfo;
import edu.berkeley.wtchoi.instrument.DexProcessor.ilbuilder.ILBuilder;
import edu.berkeley.wtchoi.instrument.DexProcessor.ilbuilder.imp.ILBuilderImp;
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.Instrument;
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.InstrumentSpecification;
import edu.berkeley.wtchoi.instrument.DexProcessor.liveRegisterAnalysis.LiveRegisterAnalysis;
import edu.berkeley.wtchoi.instrument.DexProcessor.printingVisitor.ApplicationPrintingVisitor;
import edu.berkeley.wtchoi.instrument.DexProcessor.spec.SpecDrone;
import edu.berkeley.wtchoi.instrument.DexProcessor.typeAnalysis.TypeAnalysis$;
import org.ow2.asmdex.*;
import org.ow2.asmdex.structureCommon.Label;
import org.ow2.asmdex.util.AsmDexifierApplicationVisitor;

import java.io.*;
import java.util.ArrayList;


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 7/25/12
 * Time: 3:26 PM
 *
 * SwiftHand Project follows BSD License
 *
 * [The "BSD license"]
 * Copyright (c) 2013 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class DexProcess {
    private static int ASM_API_VERSION = Opcodes.ASM4;

    private Configuration config;
    private ApplicationReader appReader;
    private ApplicationInfo appInfo;
    private ApkInfo apkInfo;
    private ILBuilder ilbuilder;
    private boolean logFlag;


    public DexProcess(Configuration config){
        this.config = config;
        logFlag = !config.flagIgnoreLog;
        apkInfo = config.apkInfo;
    }


    public boolean run() throws IOException{
        init();

        if(config.flagNoInstrument){
            noInstrument();
            return true;
        }

        //TO REPRODUCE UNALIGNED SWITCH,
        //DO RANDOM NOP INSERTION
        if(config.flagTestUnaligned){
            testUnaligned();
            return true;
        }

        if(config.flagPrintBeforeParsing){
            System.out.println("Printing Original Dex content");
            print("original");
        }

        if(config.flagStopBeforeParsing)
            return false;

        //=================================
        //Load application and parse binary
        //=================================
        buildIL();

        if(config.flagExportDotAfterBuilder)
            appInfo.exportToDot(config.getLogDirPath("dot/il"));

        if(config.flagStopAfterILBuilder){
            print("original", appInfo);
            return false;
        }

        //======================
        //Live Register Analysis
        //======================
        analyzeLiveRegister();

        if(config.flagExportDotAfterLiveAnalysis)
            appInfo.exportToDot(config.getLogDirPath("dot/live"));


        if(config.flagStopAfterLiveAnalysis){
            print("original", appInfo);
            return false;
        }


        //======================
        //Register Type Analysis
        //======================
        inferRegisterType();

        if(config.flagExportDotAfterTypeAnalysis)
            appInfo.exportToDot(config.getLogDirPath("dot/type"));

        if(config.flagStopAfterTypeAnalysis){
            print("original", appInfo);
            return false;
        }

        //===============================
        //Instrumentation
        //===============================
        instrument();
        print("original-annot", appInfo);

        return true;
    }


    private void init() throws IOException{
        appReader = new ApplicationReader(ASM_API_VERSION, config.inputFileStream);


    }


    private void print(String prefix) throws IOException {
        String pathEventLog = config.getLogPath(prefix + ".events");
        EchoingWriter eventWriter = EchoingWriter.create(pathEventLog, logFlag, config.flagVerboseILBuilder);
        Debug.registerAbortListener(new WriterFlushingWrapper(eventWriter));
        ApplicationVisitor apv = new ApplicationPrintingVisitor(ASM_API_VERSION, eventWriter);
        appReader.accept(apv, 0);

        printDexifier(prefix);
    }

    private void printDexifier(String prefix) throws IOException{
        FileWriter dexWriter = new FileWriter(config.getLogPath(prefix + ".dexifyer"));
        Debug.registerAbortListener(new WriterFlushingWrapper(dexWriter));
        AsmDexifierApplicationVisitor.setDexFileName("apk");
        AsmDexifierApplicationVisitor adav = new AsmDexifierApplicationVisitor(ASM_API_VERSION, new PrintWriter(dexWriter));

        appReader.accept(adav, 0);
    }

    private void print(String prefix, ApplicationInfo appInfo) throws IOException {
        String pathEventLog = config.getLogPath(prefix + ".events");
        EchoingWriter eventWriter = EchoingWriter.create(pathEventLog, logFlag, config.flagVerboseILBuilder);
        Debug.registerAbortListener(new WriterFlushingWrapper(eventWriter));
        ApplicationVisitor apv = new AnnotatedApplicationPrintingVisitor(ASM_API_VERSION, null, eventWriter, appInfo);
        appReader.accept(apv, 0);

        printDexifier(prefix);
    }


    private void buildIL() throws IOException{
        System.out.println("Starting il generation");
        ilbuilder = new ILBuilderImp();

        //Generate ILBuilderApplicationVisitor
        ApplicationVisitor ilb = ilbuilder.getApplicationVisitor(ASM_API_VERSION, new ManifestInfo(config.manifestInfo));

        // Feed Pretty Printer with application parsing RESULT
        String pathLoadLog = config.getLogPath("build.log");
        EchoingWriter logWriter = EchoingWriter.create(pathLoadLog, logFlag, config.flagVerboseILBuilder);

        Debug.pushRedirection(logWriter);
        appReader.accept(ilb, 0);
        Debug.popRedirection();

        appInfo = ilbuilder.getIL();

        apkInfo.mAppMainActivity = appInfo.manifest().mainActivityClassName();
        apkInfo.mAppMainPackage = appInfo.manifest().packageName();
        apkInfo.mInstructionCount = appInfo.getInstructionCount();
        apkInfo.mClassCount = appInfo.getClassCount();
        apkInfo.mMethodCount = appInfo.getMethodCount();
    }


    private void analyzeLiveRegister() throws IOException{
        System.out.println("Starting live register analysis");

        String pathLiveAnalysisLog = config.getLogPath("liveness.log");
        EchoingWriter ww = EchoingWriter.create(pathLiveAnalysisLog, logFlag, config.flagVerboseLiveAnalysis);

        Debug.pushRedirection(ww);
        LiveRegisterAnalysis.analyzeApp(appInfo);
        Debug.popRedirection();
    }




    public void inferRegisterType() throws IOException{
        System.out.println("Starting register type inference");

        String pathTypeAnalysisLog = config.getLogPath("type.log");
        EchoingWriter w = EchoingWriter.create(pathTypeAnalysisLog, logFlag, config.flagVerboseTypeAnalysis);

        Debug.pushRedirection(w);
        TypeAnalysis$.MODULE$.analyzeApp(appInfo);
        Debug.popRedirection();
    }



    private void instrument() throws IOException {
        System.out.println("Starting instrumentation");

        //create ApplicationWriter
        ApplicationWriter aw = new ApplicationWriter();

        //connect ApplicationWriter and PrintingVisitor
        String pathEventLog = config.getLogPath("modified.events");
        EchoingWriter eventWriter = EchoingWriter.create(pathEventLog, logFlag, config.flagVerboseInstrumentation);
        ApplicationVisitor eventLogVisitor = new ApplicationPrintingVisitor(ASM_API_VERSION,aw,eventWriter);


        //Generate library parsers
        ArrayList<ApplicationReader> lrl = new ArrayList<ApplicationReader>();
        for(InputStream ls: config.libFileStreams){
            lrl.add(new ApplicationReader(ASM_API_VERSION, ls));
        }
        ApplicationReader[] lrs = lrl.toArray(new ApplicationReader[lrl.size()]);


        //Get specification
        InstrumentSpecification spec = new SpecDrone();


        //connect PrintingVisitor and InstrumentingVisitor
        ApplicationVisitor iv = Instrument.getApplicationVisitor(appInfo, ASM_API_VERSION, lrs, eventLogVisitor, spec);

        String pathInstrumentLog = config.getLogPath("instrument.log");
        EchoingWriter instrumentLogWriter = EchoingWriter.create(pathInstrumentLog, logFlag, config.flagVerboseInstrumentation);

        //connect InstrumentingVisitor and ApplicationReader
        Debug.pushRedirection(instrumentLogWriter);
        appReader.accept(iv, 0);
        Debug.popRedirection();

        //write out final result
        byte[] b = aw.toByteArray();
        config.outputFileStream.write(b);

        if(config.flagVerifyPrint){
            System.out.println("Start verify print");

            File outputTempFile = File.createTempFile("class","dex");
            FileOutputStream fos = new FileOutputStream(outputTempFile);
            fos.write(b);
            fos.close();

            ApplicationReader ar2 = new ApplicationReader(ASM_API_VERSION, outputTempFile);

            EchoingWriter w4 = EchoingWriter.create(config.getLogPath("final.events"), !config.flagIgnoreLog, false);

            ApplicationVisitor mav3 = new ApplicationPrintingVisitor(ASM_API_VERSION, w4);
            ar2.accept(mav3, 0);
        }
    }

    public void noInstrument() throws IOException{
        ApplicationWriter aw = new ApplicationWriter();
        appReader.accept(aw, 0);

        //write out final result
        byte[] b = aw.toByteArray();
        config.outputFileStream.write(b);
    }

    public void testUnaligned() throws IOException{
        ApplicationWriter aw = new ApplicationWriter();
        ApplicationVisitor av = new ApplicationVisitor(ASM_API_VERSION, aw) {
            @Override
            public ClassVisitor visitClass(int access, String name, String[] signature, String superName, String[] interfaces) {
                return new ClassVisitor(ASM_API_VERSION, super.visitClass(access, name, signature, superName, interfaces)) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc, String[] signature, String[] exceptions) {
                        return new MethodVisitor(ASM_API_VERSION, super.visitMethod(access, name, desc, signature, exceptions)) {
                            @Override
                            public void visitInsn(int opcode) {
                                super.visitInsn(Opcodes.INSN_NOP);
                                super.visitLabel(new Label());
                                super.visitInsn(opcode);
                            }

                            /*
                            @Override
                            public void visitIntInsn(int opcode, int register) {
                                super.visitInsn(Opcodes.INSN_NOP);
                                super.visitIntInsn(opcode, register);
                            }
                            */


                            @Override
                            public void visitVarInsn(int opcode, int destinationRegister, int var) {
                                super.visitInsn(Opcodes.INSN_NOP);
                                super.visitLabel(new Label());
                                super.visitVarInsn(opcode, destinationRegister, var);
                            }


                            @Override
                            public void visitVarInsn(int opcode, int destinationRegister, long var) {
                                super.visitInsn(Opcodes.INSN_NOP);
                                super.visitLabel(new Label());
                                super.visitVarInsn(opcode, destinationRegister, var);
                            }

                            @Override
                            public void visitMethodInsn(int opcode, String owner, String name, String desc, int[] arguments) {
                                super.visitInsn(Opcodes.INSN_NOP);
                                super.visitLabel(new Label());
                                super.visitMethodInsn(opcode, owner, name, desc, arguments);
                            }

                            @Override
                            public void visitStringInsn(int opcode, int destinationRegister, String string) {
                                super.visitInsn(Opcodes.INSN_NOP);
                                super.visitLabel(new Label());
                                super.visitStringInsn(opcode, destinationRegister, string);
                            }

                            @Override
                            public void visitJumpInsn(int opcode, Label label, int registerA, int registerB) {
                                super.visitInsn(Opcodes.INSN_NOP);
                                super.visitLabel(new Label());
                                super.visitJumpInsn(opcode, label, registerA, registerB);
                            }
                        };
                    }
                };
            }
        };
        appReader.accept(av, 0);

        //write out final result
        byte[] b = aw.toByteArray();
        config.outputFileStream.write(b);
    }
}


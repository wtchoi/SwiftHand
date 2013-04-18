package edu.berkeley.wtchoi.instrument.DexProcessor;

import edu.berkeley.wtchoi.instrument.ApkInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/3/12
 * Time: 4:03 PM
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
public class Configuration {
    public static int VERBOSE_BUILDER = 0x2;
    public static int VERBOSE_LIVE_ANALYSIS = 0x4;
    public static int VERBOSE_TYPE_ANALYSIS = 0x8;
    public static int VERBOSE_INSTRUMENTATION = 0x10;
    public static int VERBOSE_ALL = 0x1f;

    public static int IGNORE_LOG = 0x20;
    public static int VERIFY_PRINT = 0x40;
    public static int PRINT_BEFORE_PARSING = 0x80;

    public static int STOP_BEFORE_PARSING = 0x100;
    public static int STOP_AFTER_BUILDER = 0x200;
    public static int STOP_AFTER_LIVE_ANALYSIS = 0x400;
    public static int STOP_AFTER_TYPE_ANALYSIS = 0x800;

    public static int EXPORT_DOT_AFTER_BUILDER = 0x1000;
    public static int EXPORT_DOT_AFTER_LIVE_ANALYSIS = 0x2000;
    public static int EXPORT_DOT_AFTER_TYPE_ANALYSIS = 0x4000;

    public static int NO_INSTRUMENT = 0x10000;
    public static int TEST_UNALIGNED = 0x20000;


    public Configuration(String pathInputFile, String pathOutputFile, List<String> pathLibraryFiles, String pathLog, int option, Manifest manifestInfo)
    throws IOException{
        this.pathInputFile = pathInputFile;
        this.pathOutputFile = pathOutputFile;
        this.pathLibraryFiles = pathLibraryFiles;
        this.pathLog = pathLog;
        this.manifestInfo = manifestInfo;
        this.option = option;
        init();
    }

    public Configuration(InputStream inputFileStream, OutputStream outputFileStream, List<InputStream> libFileStreams, String pathLog, int option, Manifest manifestInfo)
    throws IOException{
        this.inputFileStream = inputFileStream;
        this.outputFileStream = outputFileStream;
        this.libFileStreams = libFileStreams;
        this.pathLog = pathLog;
        this.manifestInfo = manifestInfo;
        this.option = option;
        init();
    }

    private void init() throws IOException{
        setFlags();
        prepareStreamsAndDirectory();
    }

    private void setFlags(){
        flagVerboseILBuilder = check(VERBOSE_BUILDER);
        flagVerboseLiveAnalysis = check(VERBOSE_LIVE_ANALYSIS);
        flagVerboseTypeAnalysis = check(VERBOSE_TYPE_ANALYSIS);
        flagVerboseInstrumentation = check(VERBOSE_INSTRUMENTATION);

        flagIgnoreLog = check(IGNORE_LOG);
        flagVerifyPrint = check(VERIFY_PRINT);
        flagPrintBeforeParsing = check(PRINT_BEFORE_PARSING);

        flagStopBeforeParsing = check(STOP_BEFORE_PARSING);
        flagStopAfterILBuilder = check(STOP_AFTER_BUILDER);
        flagStopAfterLiveAnalysis = check(STOP_AFTER_LIVE_ANALYSIS);
        flagStopAfterTypeAnalysis = check(STOP_AFTER_TYPE_ANALYSIS);

        flagExportDotAfterBuilder = check(EXPORT_DOT_AFTER_BUILDER);
        flagExportDotAfterLiveAnalysis = check(EXPORT_DOT_AFTER_LIVE_ANALYSIS);
        flagExportDotAfterTypeAnalysis = check(EXPORT_DOT_AFTER_TYPE_ANALYSIS);

        flagNoInstrument = check(NO_INSTRUMENT);
        flagTestUnaligned = check(TEST_UNALIGNED);
    }

    private boolean check(int flag){
        int band = (option & flag);
        return band > 0;
    }
    
    public String pathInputFile = null;
    public String pathOutputFile = null;
    public List<String> pathLibraryFiles = null;
    private int option = 0;

    public Manifest manifestInfo = null;

    public InputStream inputFileStream = null;
    public OutputStream outputFileStream = null;
    public List<InputStream> libFileStreams = null;

    public String pathLog = null;

    public boolean flagVerboseILBuilder = false;
    public boolean flagVerboseLiveAnalysis = false;
    public boolean flagVerboseTypeAnalysis = false;
    public boolean flagVerboseInstrumentation = false;

    public boolean flagIgnoreLog = false;
    public boolean flagVerifyPrint = false;
    public boolean flagPrintBeforeParsing = false;

    public boolean flagStopBeforeParsing = false;
    public boolean flagStopAfterILBuilder = false;
    public boolean flagStopAfterLiveAnalysis = false;
    public boolean flagStopAfterTypeAnalysis = false;

    public boolean flagExportDotAfterBuilder = false;
    public boolean flagExportDotAfterLiveAnalysis = false;
    public boolean flagExportDotAfterTypeAnalysis = false;

    public boolean flagNoInstrument = false;
    public boolean flagTestUnaligned = false;

    public ApkInfo apkInfo;

    public String getLogPath(String filename){
        return pathLog + "/" + filename;
    }

    public String getLogDirPath(String subDirectoryPath){
        String path = pathLog + "/" + subDirectoryPath;
        File directory = new File(path);

        if(directory.exists() && !directory.isDirectory()){
            throw new RuntimeException(path + " already exists and is not a directory");
        }

        if(!directory.exists()){
            if(!directory.mkdirs())
                throw new RuntimeException("Cannot create directory : " + path);
        }

        return path;
    }


    public void closeStreams(){
        try{
            outputFileStream.close();
            inputFileStream.close();
            if(libFileStreams != null){
                for(InputStream lib:libFileStreams){

                }
            }
        }
        catch(Exception e){}
    }

    private void prepareStreamsAndDirectory() throws IOException{
        if(inputFileStream == null){
            if(pathInputFile != null){
                File inFile = new File(pathInputFile);
                if(!inFile.exists()){
                    throw new RuntimeException("input file does not exist!");
                }
                System.out.println(inFile.length());
                this.inputFileStream = new FileInputStream(this.pathInputFile);
            }
            else{
                throw new RuntimeException("Either input file stream or name of input file must be provided");
            }
        }

        if(flagStopBeforeParsing) return;

        if(outputFileStream == null){
            if(pathOutputFile != null){
                this.outputFileStream = new FileOutputStream(pathOutputFile);
            }
            throw new RuntimeException("Either output file stream or name of output file must be provided");
        }


        if(pathLibraryFiles != null){
            libFileStreams = new ArrayList<InputStream>();
            for(String path:pathLibraryFiles){
                FileInputStream fis = new FileInputStream(path);
                libFileStreams.add(fis);
            }
        }

        File logDir = new File(pathLog);
        if(logDir.exists()){
            if(!logDir.isDirectory())
                throw new RuntimeException(pathLog + " exist and it not a directory");
        }
        else if(!logDir.mkdirs()){
            throw new RuntimeException("cannot create directory");
        }
    }
}

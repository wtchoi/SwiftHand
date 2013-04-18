package edu.berkeley.wtchoi.instrument;

import com.google.gson.Gson;
import edu.berkeley.wtchoi.instrument.ApkProcessor.ApkProcessor;
import sun.security.tools.JarSigner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/23/12
 * Time: 12:42 AM
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
public class Instrument {
    public static void main(String args[]){

        String keystoreFileName;
        String libJarFileName;

        Vector<String> inputs =new Vector<String>();

        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/tippy/Tippy Tipper.apk");
        inputs.add("/Users/wtchoi/work/Instrumentation/test/target/weight-chart/out/production/Weight-chart/Weight-chart.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/android-mileage/Trunk.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/sanity/cri.sanity_21100.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/chordReader/com.nolanlawson.chordreader_8.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/whohas/de.freewarepoint.whohasmystuff_8.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/dailymoney/com.bottleworks.dailymoney_2012110700.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/dalvikExplorer/org.jessies.dalvikexplorer_34.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/myfinance/org.totschnig.myexpenses_34.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/mininote/jp.gr.java_conf.hatalab.mnv_40.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/anymemo/AnyMemo.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/learnMusicNote/LMN.apk");

        //======================================
        //Applications with compilation issues
        //======================================
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/hex/hex.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/mobileorg/mobileorg_98.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/diedroid/net.logomancy.diedroid_9.apk");

        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/wikipedia/org.wikipedia_23.apk");

        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/passwordmakerPro/org.passwordmaker.android_7.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/acal/com.morphoss.acal_60.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/addi/com.addi_44.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/alarmklock/Alarm Klock_1.7.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/sokoban/com.mobilepearls.sokoban_12.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/tramhunter/com.andybotting.tramhunter_1100.apk");

        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/carreport/me.kuehle.carreport_17.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/androzoic/com.androzic_85.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/bible/net.bible.android.activity_93.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/moloko/dev.drsoran.moloko_94210.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/gnucash/org.gnucash.android_1.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/bookcatalog/com.eleybourn.bookcatalogue_124.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/majhong/com.anoshenko.android.mahjongg_14.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/hotdeath/com.smorgasbork.hotdeath_8.apk");



        //======================================
        //Application inadequate
        //======================================
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/enumbers/bin/org.uaraven.e.EMainActivity.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/dictionary/aarddict.android_13.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/andquote/net.progval.android.andquote_6.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/nanoconverter/com.nanoconverter.zlab_38.apk");


        //======================================
        //Applications too simple to be tested
        //======================================
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/rpn/com.ath0.rpn_17.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/note/NotepadClean.apk");
        //inputs.add("/Users/wtchoi/work/Instrumentation/test/target/searchlight/com.scottmain.android.searchlight_4.apk");

        libJarFileName = "/Users/wtchoi/work/ChimpChat/out/artifacts/Shared_jar/Shared.jar";
        keystoreFileName = "/Users/wtchoi/work/Instrumentation/test/debug.keystore";

        Mode mode = Mode.Full;
        boolean flagVerifyPrint = false;
        for(String inputApk:inputs){
            run(inputApk, libJarFileName, keystoreFileName, mode, flagVerifyPrint);
        }
    }

    enum Mode{
        Print,
        TestUnaligned,
        TestILBuild,
        TestLiveness,
        TestTypeInference,
        TestInstrument,
        Full,
        FullVerbose,
        FullSilent
    }

    public static void run(String inputApkFileName, String libJarFileName, String keystoreFileName, Mode mode, boolean flagVerifyPrint){
        String outputApkFileName = inputApkFileName.replaceFirst(".apk",".modified.apk");

        try{
            File tempLibDexFile = File.createTempFile("dronlib",".dex");
            String libDexFileName = tempLibDexFile.getAbsolutePath();

            String[] dxArgs = {"--dex", "--output="+libDexFileName, libJarFileName};
            com.android.dx.command.Main.main(dxArgs);

            ApkInfo apkInfo = new ApkInfo();
            apkInfo.mApkPath = inputApkFileName;

            ApkProcessor apkProcessor = new ApkProcessor();

            FileInputStream libInputStream = new FileInputStream(libDexFileName);


            MyXmlTransformer manifestTransformer = new MyXmlTransformer();
            MyDexTransformer dexTransformer = new MyDexTransformer(libInputStream, apkInfo);

            switch(mode){
                case Print:
                    dexTransformer.usePrintMode();
                    manifestTransformer.usePrintMode();
                    break;
                case TestUnaligned:
                    dexTransformer.useTestUnaligned();
                    manifestTransformer.usePrintMode();
                case TestILBuild:
                    dexTransformer.useILBuildMode();
                    break;
                case TestLiveness:
                    dexTransformer.useLivenessMode();
                    break;
                case TestTypeInference:
                    dexTransformer.useTypeMode();
                    break;
                case TestInstrument:
                    dexTransformer.useInstrumentMode();
                    break;
                case FullSilent:
                    dexTransformer.useSilentMode();
                    break;
                case FullVerbose:
                    dexTransformer.useFullVerboseMode();
                case Full:
                    break;
                default:
                    break;
            }


            manifestTransformer.setListener(dexTransformer);
            apkProcessor.instrument(inputApkFileName, outputApkFileName, manifestTransformer, dexTransformer);

            if(mode != Mode.Print)
                apkInfo.loadInstrumentationResults();

            String[] jsArgs = {"-keystore", keystoreFileName, "-storepass", "android", "-keypass", "android", outputApkFileName, "androiddebugkey"};
            JarSigner jarSigner = new JarSigner();
            jarSigner.run(jsArgs);


            if (flagVerifyPrint){
                apkProcessor.print(inputApkFileName.replace(".apk", ".modified.apk"));
            }

            if(mode != Mode.Print){
                final Gson gson = new Gson();
                //FileWriter writer = new FileWriter(inputApkFileName + ".json");
                FileWriter writer = new FileWriter(inputApkFileName.replace(".apk", ".modified.json"));
                writer.write(gson.toJson(apkInfo));
                writer.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Cannot finish apk modification");
        }
    }
}



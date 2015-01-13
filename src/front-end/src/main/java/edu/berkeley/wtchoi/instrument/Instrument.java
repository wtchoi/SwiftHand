package edu.berkeley.wtchoi.instrument;

import com.google.gson.Gson;
import edu.berkeley.wtchoi.instrument.ApkProcessor.ApkProcessor;
import sun.security.tools.JarSigner;
import java.security.KeyStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public static void run(String inputApkFileName, String keystoreFileName, String libJarFileName, Mode mode, boolean flagVerifyPrint){
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

            String keystorePassword = "swifthandKeystorePass";
            String keyAlias = "swifthandKey";
            String keyPassword = "swifthandKeyPass";

            //check whether KeyStore file exists
            File keystoreFile = new File(keystoreFileName);
            if(!keystoreFile.exists()) throw new RuntimeException("key store file does not exists!");

            String[] jsArgs = {"-sigalg", "SHA1withRSA", "-digestalg", "SHA1", "-keystore", keystoreFileName, "-storepass", keystorePassword, "-keypass", keyPassword, outputApkFileName, keyAlias};
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



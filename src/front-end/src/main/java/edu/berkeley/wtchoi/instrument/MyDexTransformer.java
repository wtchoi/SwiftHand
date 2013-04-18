package edu.berkeley.wtchoi.instrument;

import edu.berkeley.wtchoi.instrument.ApkProcessor.ApkProcessor;
import edu.berkeley.wtchoi.instrument.DexProcessor.Configuration;
import edu.berkeley.wtchoi.instrument.DexProcessor.DexProcess;
import edu.berkeley.wtchoi.instrument.DexProcessor.Manifest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/23/12
 * Time: 1:59 AM
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
public class MyDexTransformer extends ApkProcessor.FileTransformer implements MyXmlTransformer.OnFinishListener{
    private InputStream libInputStream;
    private Manifest manifestInfo;
    private ApkInfo apkInfo;

    public MyDexTransformer(InputStream libInputStream, ApkInfo apkInfo){
        this.libInputStream = libInputStream;
        this.apkInfo = apkInfo;
    }

    private boolean flagPrintMode = false;
    private boolean flagTestLiveness = false;
    private boolean flagTestType = false;
    private boolean flagTestInstrument = false;
    private boolean flagSilentMode = false;
    private boolean flagTestBuild = false;
    private boolean flagTestUnaligned = false;
    private boolean flagFullVerbose = false;

    private OnFinishListener listener = null;

    public void setOnFinishListener(OnFinishListener l){
        listener = l;
    }

    public void usePrintMode(){
        flagPrintMode = true;
    }

    public void useTestUnaligned(){
        flagTestUnaligned = true;
    }

    public void useILBuildMode(){
        flagTestBuild = true;
    }

    public void useLivenessMode(){
        flagTestLiveness = true;
    }

    public void useTypeMode(){
        flagTestType = true;
    }

    public void useInstrumentMode(){
        flagTestInstrument = true;
    }

    public void useSilentMode(){
        flagSilentMode = true;
    }

    public void useFullVerboseMode(){
        flagFullVerbose = true;
    }

    @Override
    public void transform(InputStream in, OutputStream out){
        try{
            List<InputStream> lsa = new ArrayList<InputStream>();
            lsa.add(libInputStream);

            int option = Configuration.VERIFY_PRINT;

            if (!flagSilentMode){
                if (flagPrintMode)
                    option = Configuration.NO_INSTRUMENT;
                else if (flagTestUnaligned)
                    option = Configuration.TEST_UNALIGNED;
                else if(flagTestBuild)
                    option = Configuration.EXPORT_DOT_AFTER_BUILDER | Configuration.STOP_AFTER_BUILDER | Configuration.VERBOSE_BUILDER;
                else if(flagTestLiveness)
                    option =  Configuration.EXPORT_DOT_AFTER_LIVE_ANALYSIS | Configuration.STOP_AFTER_LIVE_ANALYSIS;
                else if(flagTestType)
                    option = Configuration.VERBOSE_TYPE_ANALYSIS |  Configuration.VERIFY_PRINT | Configuration.EXPORT_DOT_AFTER_TYPE_ANALYSIS | Configuration.STOP_AFTER_TYPE_ANALYSIS;
                else if(flagTestInstrument)
                    option =  Configuration.EXPORT_DOT_AFTER_TYPE_ANALYSIS | Configuration.VERBOSE_INSTRUMENTATION | Configuration.VERIFY_PRINT;
                else if(flagFullVerbose)
                    option = Configuration.PRINT_BEFORE_PARSING | Configuration.EXPORT_DOT_AFTER_BUILDER | Configuration.EXPORT_DOT_AFTER_LIVE_ANALYSIS | Configuration.EXPORT_DOT_AFTER_TYPE_ANALYSIS | Configuration.VERIFY_PRINT;
                else
                    option = Configuration.PRINT_BEFORE_PARSING | Configuration.EXPORT_DOT_AFTER_TYPE_ANALYSIS | Configuration.VERIFY_PRINT;
            }

            String apkPath = apkInfo.mApkPath;
            Configuration config = new Configuration(in, out, lsa, apkPath.substring(0, apkPath.lastIndexOf("/")), option, manifestInfo);
            config.apkInfo = apkInfo;

            DexProcess dp = new DexProcess(config);
            dp.run();

            if(listener != null)
                listener.onFinish(dp);
        }
        catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("Cannot finish transform");
        }
    }

    @Override
    public void onFinish(AxmlDecoder ad){
        this.manifestInfo = ad.manifest;
    }

    public static interface OnFinishListener{
        public void onFinish(DexProcess dp);
    }
}

package edu.berkeley.wtchoi.swift.testing.android.runners;

import edu.berkeley.wtchoi.swift.testing.android.ApkInfo;
import edu.berkeley.wtchoi.swift.testing.android.TargetApplication;
import edu.berkeley.wtchoi.swift.util.OptionUtil;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/5/13
 * Time: 1:52 AM
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
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
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
public class TestRunnerOption {

    /* Options for LearningGuide */
    final public static int MERGE_DEFAULT  = 0x00;
    final public static int MERGE_ANCESTOR = 0x01;
    final public static int MERGE_NEAREST  = 0x02;
    final public static int MERGE_HYBRID   = 0x04;

    final public static int DRIVE_DEFAULT = 0x0;
    final public static int DRIVE_UNREALIZED_PATH = 0x10;

    final public static int PLOT_MERGE_STEPS = 0x100;

    /* Options for RandomGuide */
    final public static int TABOO_SET = 0x01;


    public String mMainPackage;
    public String mMainActivity;
    public String mApkPath;
    public long timeout;
    public String postfix;
    public String resultPath;
    public ApkInfo apkInfo;
    public String testName = null;
    public String deviceID = ".*"; //default : any device
    public int localPort = 13337;

    public int randomSeed;
    public boolean printAppState = false;
    public int summaryDumpCycle = 1;
    public int intermediateDumpCycle = 1;
    public int necessaryStableInterval = 2100;
    public int transitionTimeout = 30000;

    public int extra = 0x0;

    public TestRunnerOption(String target, long timeout, String postfix, String resultPath){
        try{
            assert(target.endsWith(".modified.apk"));

            ApkInfo apkInfo = ApkInfo.fromJson(target.replace(".apk",".json"));
            //ApkInfo apkInfo = ApkInfo.fromJson(target + ".json");

            String mMainActivity = apkInfo.mAppMainActivity;

            if(mMainActivity.startsWith("."))
                mMainActivity = mMainActivity.substring(1,mMainActivity.length());

            String mMainPackage = apkInfo.mAppMainPackage;
            String mTestApkPath = target; //target.replace(".apk",".modified.apk");

            this.mMainActivity = mMainActivity;
            this.mMainPackage = mMainPackage;
            this.mApkPath = mTestApkPath;
            this.timeout = timeout;
            this.postfix = postfix;
            this.resultPath = resultPath;
            this.apkInfo = apkInfo;
        }
        catch(Exception e){
            new RuntimeException("Cannot read " + target + ".json file!", e);
        }

    }

    public String getTestName(){
        if(testName == null)
            return mMainPackage + (postfix != null ? ("_" + postfix) : "");
        else
            return testName + (postfix != null ? ("_" + postfix) : "");
    }

    public void addExtra(int mode){
        extra = extra | mode;
    }

    public TargetApplication.Option toAppOption(){
        TargetApplication.Option appOpt = new TargetApplication.Option();

        appOpt.pkg = this.mMainPackage;
        appOpt.mainActivity = this.mMainActivity;
        appOpt.binaryPath = this.mApkPath;
        appOpt.deviceID = this.deviceID;
        appOpt.localPort = this.localPort;
        appOpt.deviceObservationTickInterval = this.necessaryStableInterval / 12;
        appOpt.transitionTimeout = this.transitionTimeout;

        return appOpt;
    }

    public boolean checkExtra(int extra){
        return OptionUtil.check(this.extra, extra);
    }
}

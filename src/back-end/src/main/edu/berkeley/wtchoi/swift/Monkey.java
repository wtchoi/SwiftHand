package edu.berkeley.wtchoi.swift;

import edu.berkeley.wtchoi.logger.Logger;
import edu.berkeley.wtchoi.logger.LoggerImp;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.PushCommand;
import edu.berkeley.wtchoi.swift.testing.android.ViewToEvents;
import edu.berkeley.wtchoi.swift.testing.android.runners.TestRunner;
import edu.berkeley.wtchoi.swift.testing.android.runners.TestRunnerFactory;
import edu.berkeley.wtchoi.swift.testing.android.runners.TestRunnerOption;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/5/13
 * Time: 3:39 AM
 *
 * SwiftHand Project follows BSD License
 *
 * [The "BSD license"]
 * Copyright (c) 2013 Wontae Choi.
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

public class Monkey {
    public static void main(String args[]) {
        Logger.init(new LoggerImp() {
            @Override
            public void log(String s) {
                System.out.println(s);
            }
        });

        Set<ICommand> defaultPalette = new TreeSet<ICommand>();
        defaultPalette.add(PushCommand.getBack());
        defaultPalette.add(PushCommand.getMenu());
        ViewToEvents.setDefaultEvents(defaultPalette);

        String resultPath = "/Users/wtchoi/work/Instrumentation/test/result/2013-03-27";
        //String resultPath = "/Users/wtchoi/work/Instrumentation/test/result/temp";
        File f = new File(resultPath);
        f.mkdirs();

        Vector<String> targetApplications = new Vector<String>();
        Set<String> packagesExcludeFromCoverage = new TreeSet<String>();

        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/tippy/Tippy Tipper.modified.apk");
        //packagesExcludeFromCoverage.add("Lcom/flurry");

        targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/android-mileage/Trunk.modified.apk");
        //packagesExcludeFromCoverage.add("Lau/com/bytecode");
        //packagesExcludeFromCoverage.add("Lcom/artfulbits/");

        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/weight-chart/out/production/Weight-chart/Weight-chart.modified.apk");

        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/sanity/cri.sanity_21100.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/learnMusicNote/LMN.modified.apk");

        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/chordReader/com.nolanlawson.chordreader_8.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/whohas/de.freewarepoint.whohasmystuff_8.modified.apk");

        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/nanoconverter/com.nanoconverter.zlab_38.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/dalvikExplorer/org.jessies.dalvikexplorer_34.modified.apk");

        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/myfinance/org.totschnig.myexpenses_34.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/mininote/jp.gr.java_conf.hatalab.mnv_40.modified.apk");

        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/anymemo/AnyMemo.modified.apk");
        //packagesExcludeFromCoverage.add("Lcom/google");
        //packagesExcludeFromCoverage.add("Loauth/signpost");
        //packagesExcludeFromCoverage.add("Lorg/amr/arabic");
        //packagesExcludeFromCoverage.add("Lorg/apache");
        //packagesExcludeFromCoverage.add("Lorg/color");

        //------------------------------------------
        // Applications too simple
        //------------------------------------------
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/searchlight/com.scottmain.android.searchlight_4.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/rpn/com.ath0.rpn_17.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/note/NotepadClean.modified.apk");

        //------------------------------------------
        // Applications with Inconsistency Issue
        //------------------------------------------
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/dailymoney/com.bottleworks.dailymoney_2012110700.modified.apk");
        ////packagesExcludeFromCoverage.add("Lorg/javia/arity");
        //packagesExcludeFromCoverage.add("Lorg/achartengine");
        //packagesExcludeFromCoverage.add("Lcom/csvreader");




        //-----------------------------------------
        // Applications with Compilation Issue
        //-----------------------------------------
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/alarmklock/Alarm Klock_1.7.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/wikipedia/org.wikipedia_23.modified.apk");

        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/androzoic/com.androzic_85.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/mobileorg/mobileorg_98.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/carreport/me.kuehle.carreport_17.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/wikipedia/org.wikipedia_23.modified.apk");
        //targetApplications.add("/Users/wtchoi/work/Instrumentation/test/target/majhong/com.anoshenko.android.mahjongg_14.modified.apk");


        Vector<String> runnerTypes = new Vector<String>();
        //runnerTypes.add("learningHU");
        //runnerTypes.add("random");
        //runnerTypes.add("lstar");
        //runnerTypes.add("learningWithScout);
        runnerTypes.add("learningAU");
        //runnerTypes.add("learningNU");
        //runnerTypes.add("interactive");


        Integer[] seeds = {1};
        long timeout = 3600;

        for(String target:targetApplications){
            for(String type : runnerTypes){
                for(int seed : seeds){
                    doTesting(target, type, seed, timeout, resultPath, packagesExcludeFromCoverage);
                }
            }
        }
        Runtime.getRuntime().halt(0);
    }

    private static void doTesting(String target, String type, int randomSeed, long timeout, String resultPath, Set<String> packagesToExclude){
        TestRunnerOption options = new TestRunnerOption(target, timeout, String.valueOf(randomSeed), resultPath);
        //options.apkInfo.setExclude(packagesToExclude);

        options.randomSeed = randomSeed;
        options.printAppState = false;
        options.summaryDumpCycle = 1;
        options.intermediateDumpCycle = 1;
        options.printAppState = true;
        options.necessaryStableInterval = 500;
        options.transitionTimeout = 30000;
        options.apkInfo.setPackageToExcludeFromFile("/Users/wtchoi/work/Instrumentation/test/target/exclude");


        options.resultPath = options.resultPath + "/" + type;
        TestRunner runner;
        if(type.equals("learningAU") || type.equals("swift")){
            options.addExtra(TestRunnerOption.MERGE_ANCESTOR);
            options.addExtra(TestRunnerOption.DRIVE_UNREALIZED_PATH);
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.ActiveLearning, options);
        }
        else if (type.equals("learningNU")){
            options.addExtra(TestRunnerOption.MERGE_NEAREST);
            options.addExtra(TestRunnerOption.DRIVE_UNREALIZED_PATH);
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.ActiveLearning, options);
        }
        else if (type.equals("learningHU")){
            options.addExtra(TestRunnerOption.MERGE_HYBRID);
            options.addExtra(TestRunnerOption.DRIVE_UNREALIZED_PATH);
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.ActiveLearning, options);
        }
        else if (type.equals("learningA")){
            options.addExtra(TestRunnerOption.MERGE_ANCESTOR);
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.ActiveLearning, options);
        }
        else if (type.equals("learningN")){
            options.addExtra(TestRunnerOption.MERGE_NEAREST);
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.ActiveLearning, options);
        }
        else if (type.equals("learningH")){
            options.addExtra(TestRunnerOption.MERGE_HYBRID);
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.ActiveLearning, options);
        }
        else if (type.equals("learningWithScout")){
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.LearningWithScout, options);
        }
        else if (type.equals("lstar")){
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.LearningLStar, options);
        }
        else if (type.equals("interactive")){
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.Interactive, options);
        }
        else if (type.equals("random")){
            runner = TestRunnerFactory.create(TestRunnerFactory.Type.Random, options);
        }
        else{
            throw new RuntimeException("Not Implemented Yet!");
        }

        runner.runTesting();
    }


    /*
    public static void runGraphTesting(int timeoutSecond, int iterationCount){
        GraphTestingObserver observer = new GraphTestingObserver(mMainPackage + "_" + iterationCount);
        observer.setDumpPath(mResultPath + "/graph");

        TargetApplication app = new TargetApplication(mMainPackage, mMainActivity, mTestApkPath, true);
        app.setObserver(observer);

        GraphGuide guide = new GraphGuide();
        guide.setObserver(observer);
        observer.intermediateDumpCycle(1);

        runTesting(guide, observer, app, timeoutSecond);
    }
    */
}


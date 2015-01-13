package edu.berkeley.wtchoi.swift;

import edu.berkeley.wtchoi.logger.Logger;
import edu.berkeley.wtchoi.logger.LoggerImp;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.PushCommand;
import edu.berkeley.wtchoi.swift.testing.android.ViewToEvents;
import edu.berkeley.wtchoi.swift.testing.android.runners.TestRunner;
import edu.berkeley.wtchoi.swift.testing.android.runners.TestRunnerFactory;
import edu.berkeley.wtchoi.swift.testing.android.runners.TestRunnerOption;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/5/13
 * Time: 3:39 AM
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
public class CommandLine {
    public static void main(String args[]){

        TestRunnerOption toption = null;
        TestRunnerFactory.Type type = null;

        try{
            String apk = args[0];
            String mode = args[1];
            String timeout = args[2];
            String randomSeed = args[3];
            String output = args[4];

            String opt[] = new String[args.length - 5];
            for(int i =0;i<args.length-5;i++)
                opt[i] = args[i+5];

            OptionParser parser = new OptionParser("n:c:i:x:d:p:s:t:v");
            OptionSet options = parser.parse(opt);

            toption = new TestRunnerOption(apk, Long.valueOf(timeout), randomSeed, output);

            toption.randomSeed = Integer.valueOf(randomSeed);

            if(options.has("n")){
                toption.testName = (String) options.valueOf("n");
                System.out.println("name = " + toption.testName);
            }

            if(options.has("c"))
                toption.summaryDumpCycle = Integer.valueOf((String) options.valueOf("c"));

            if(options.has("i"))
                toption.intermediateDumpCycle = Integer.valueOf((String) options.valueOf("i"));

            if(options.has("v"))
                toption.printAppState = true;

            if(options.has("x"))
                toption.apkInfo.setPackageToExcludeFromFile((String)options.valueOf("x"));

            if(options.has("d"))
                toption.deviceID = (String) options.valueOf("d");

            if(options.has("p"))
                toption.localPort = Integer.valueOf((String) options.valueOf("p"));

            if(options.has("s"))
                toption.necessaryStableInterval = Integer.valueOf((String) options.valueOf("s"));

            if(options.has("t"))
                toption.transitionTimeout = Integer.valueOf((String) options.valueOf("t"));

            type = parseMode(mode, toption);
        }
        catch(Exception e){
            e.printStackTrace();
            Runtime.getRuntime().halt(1);
        }


        TestRunner runner = TestRunnerFactory.create(type, toption);

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


        try{
            runner.runTesting();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Runtime.getRuntime().halt(0);
    }

    private static TestRunnerFactory.Type parseMode(String mode, TestRunnerOption option){
        if(mode.equals("random")){
            return TestRunnerFactory.Type.Random;
        }
        else if(mode.equals("interactive")){
            return TestRunnerFactory.Type.Interactive;
        }
        else if(mode.equals("learningAU") || mode.equals("swift")){
            option.addExtra(TestRunnerOption.DRIVE_UNREALIZED_PATH);
            option.addExtra(TestRunnerOption.MERGE_ANCESTOR);
            return TestRunnerFactory.Type.ActiveLearning;
        }
        else if(mode.equals("learningNU")){
            option.addExtra(TestRunnerOption.DRIVE_UNREALIZED_PATH);
            option.addExtra(TestRunnerOption.MERGE_NEAREST);
            return TestRunnerFactory.Type.ActiveLearning;
        }
        else if(mode.equals("learningHU")){
            option.addExtra(TestRunnerOption.DRIVE_UNREALIZED_PATH);
            option.addExtra(TestRunnerOption.MERGE_HYBRID);
            return TestRunnerFactory.Type.ActiveLearning;
        }
        else if(mode.equals("learningA")){
            option.addExtra(TestRunnerOption.MERGE_ANCESTOR);
            return TestRunnerFactory.Type.ActiveLearning;
        }
        else if(mode.equals("learningN")){
            option.addExtra(TestRunnerOption.MERGE_NEAREST);
            return TestRunnerFactory.Type.ActiveLearning;
        }
        else if(mode.equals("learningH")){
            option.addExtra(TestRunnerOption.MERGE_HYBRID);
            return TestRunnerFactory.Type.ActiveLearning;
        }
        /*
        else if(mode.compareTo("learningWithScout") == 0){
            throw new RuntimeException("This mode is not available yet")
            return TestRunnerFactory.Type.LearningWithScout;
        }
        */
        else if(mode.equals("lstar")){
            return TestRunnerFactory.Type.LearningLStar;
        }


        throw new RuntimeException("mode \"" + mode + "\" is not supported");
    }
}

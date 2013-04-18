package edu.berkeley.wtchoi.swift.testing.android.runners;

import edu.berkeley.wtchoi.swift.testing.Testing;
import edu.berkeley.wtchoi.swift.testing.android.*;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/19/13
 * Time: 4:43 PM
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
public abstract class TestRunner {
    abstract protected AppTestingGuide getGuide();
    abstract protected AppTestingObserver getObserver();
    abstract protected TargetApplication getApp();

    abstract protected void prepareTesting();
    abstract protected void setParameters();

    public TestRunner(TestRunnerOption option){
        this.option = option;
    }

    TestRunnerOption option;

    protected Set<String> packageToExclude;

    private boolean useRecord = false;
    private boolean useRecording = false;

    protected String getTestName(){
        return option.getTestName();
    }

    public void useRecord(){
        useRecord = true;
    }

    public void useRecording(){
        useRecording = true;
    }


    public void runTesting(){
        prepareTesting();

        AppTestingObserver observer = getObserver();
        AppTestingGuide guide = getGuide();
        TargetApplication app = getApp();

        observer.setGuide(guide);
        observer.setApkInfo(option.apkInfo);
        observer.setDumpPath(option.resultPath);
        observer.summaryDumpCycle(option.summaryDumpCycle);
        observer.intermediateDumpCycle(option.intermediateDumpCycle);

        app.setObserver(observer);
        app.setApkInfo(option.apkInfo);

        if(option.printAppState)
            app.useStatePrint();

        app.setChannelTimeout(100000);

        setParameters();

        String recordpath = option.resultPath +  "/" + option.mMainPackage + ".record";
        SequentialRecorder recorder = null;
        SequentialRecord record = null;

        if(useRecord){
            record = new SequentialRecord(recordpath);
            getApp().setRecord(record);
        }

        if(useRecording){
            recorder = new SequentialRecorder(recordpath);
            getApp().setRecorder(recorder);
        }

        Testing testing = new Testing();
        testing.addObserver(getObserver());
        testing.setGuide(getGuide());
        testing.setTarget(getApp());
        testing.setTimeout(option.timeout * 1000);
        testing.run();

        if(useRecording){
            recorder.close();
        }
    }
}

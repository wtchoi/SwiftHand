package edu.berkeley.wtchoi.swift.testing.android.runners;

import edu.berkeley.wtchoi.swift.testing.android.AppTestingGuide;
import edu.berkeley.wtchoi.swift.testing.android.AppTestingObserver;
import edu.berkeley.wtchoi.swift.testing.android.TargetApplication;
import edu.berkeley.wtchoi.swift.testing.android.learning.LearningTestingObserver;
import edu.berkeley.wtchoi.swift.testing.android.learning.LearningWithScoutGuide;
import edu.berkeley.wtchoi.swift.util.OptionUtil;
import edu.berkeley.wtchoi.swift.util.RandomUtil;

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
public class SwitchingLearningTestRunner extends TestRunner {
    public SwitchingLearningTestRunner(TestRunnerOption option){
        super(option);
        RandomUtil.initSharedInstance(option.randomSeed);
    }

    protected void prepareTesting(){
        observer = new LearningTestingObserver(getTestName());

        TargetApplication.Option appOpt = option.toAppOption();
        app = new TargetApplication(appOpt);

        guide = new LearningWithScoutGuide();
    }

    protected void setParameters(){
        guide.setLearningLog(option.resultPath, getTestName());
        guide.setObserver(observer);
        guide.useScreenBasedGoalSelection();
        guide.useShallowTransitionBasedOptimization();

        if(OptionUtil.check(option.extra, TestRunnerOption.PLOT_MERGE_STEPS))
            guide.plotMergeSteps();
    }

    private LearningTestingObserver observer;
    private LearningWithScoutGuide guide;
    private TargetApplication app;

    protected AppTestingGuide getGuide(){ return guide; }
    protected AppTestingObserver getObserver(){ return observer; }
    protected TargetApplication getApp(){ return app; }
}

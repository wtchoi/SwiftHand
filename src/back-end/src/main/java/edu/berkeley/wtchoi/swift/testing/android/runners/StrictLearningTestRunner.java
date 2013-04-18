package edu.berkeley.wtchoi.swift.testing.android.runners;

import edu.berkeley.wtchoi.swift.testing.android.AppTestingGuide;
import edu.berkeley.wtchoi.swift.testing.android.AppTestingObserver;
import edu.berkeley.wtchoi.swift.testing.android.TargetApplication;
import edu.berkeley.wtchoi.swift.testing.android.learning.ActiveLearningGuide;
import edu.berkeley.wtchoi.swift.testing.android.learning.LearningTestingObserver;
import edu.berkeley.wtchoi.swift.util.RandomUtil;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/25/13
 * Time: 4:00 AM
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
public class StrictLearningTestRunner extends TestRunner {
    public StrictLearningTestRunner(TestRunnerOption option){
        super(option);
        RandomUtil.initSharedInstance(option.randomSeed);
    }

    protected void prepareTesting(){
        observer = new LearningTestingObserver(getTestName());

        TargetApplication.Option appOpt = option.toAppOption();
        app = new TargetApplication(appOpt);

        guide = new ActiveLearningGuide();
    }

    protected void setParameters(){
        guide.setLearningLog(option.resultPath, getTestName());
        guide.setObserver(observer);

        if(option.checkExtra(TestRunnerOption.DRIVE_UNREALIZED_PATH))
            guide.setDriveOption(ActiveLearningGuide.DriveOption.UseUnrealizedPath);

        if(option.checkExtra(TestRunnerOption.MERGE_ANCESTOR))
            guide.setMergeOption(ActiveLearningGuide.MergeOption.ToAncestor);
        else if(option.checkExtra(TestRunnerOption.MERGE_NEAREST))
            guide.setMergeOption(ActiveLearningGuide.MergeOption.ToNearest);
        else if(option.checkExtra(TestRunnerOption.MERGE_HYBRID))
            guide.setMergeOption(ActiveLearningGuide.MergeOption.Hybrid);

        if(option.checkExtra(TestRunnerOption.PLOT_MERGE_STEPS))
            guide.plotMergeSteps();
    }

    private LearningTestingObserver observer;
    private ActiveLearningGuide guide;
    private TargetApplication app;

    protected AppTestingGuide getGuide(){ return guide; }
    protected AppTestingObserver getObserver(){ return observer; }
    protected TargetApplication getApp(){ return app; }
}

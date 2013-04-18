package edu.berkeley.wtchoi.swift.testing.android.learning;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.android.AppRequest;
import edu.berkeley.wtchoi.swift.testing.android.AppResult;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.State;
import edu.berkeley.wtchoi.swift.util.RandomUtil;
import edu.berkeley.wtchoi.collection.CSet;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/28/13
 * Time: 6:15 PM
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

//Learning Guide Version2 :
// Exploration is guided by combination of model and random search
public class LearningWithScoutGuide extends AbstractLearningGuide {
    private int explorationCount = 0;
    private final int explorationLimit = 5;

    private boolean screenBasedGoalSelection = false;
    private ScreenBasedWeightCalculator sbwc;

    private boolean shallowTransitionBasedOptimization = false;
    private boolean executionTowardNewEvent = false;

    private ExecutionFollower executionFollower;


    Mode mode;
    enum Mode{
        Drive("Drive"),
        Explore("Explore"),
        ReadyForNextGoal("DrivingFinished");

        final private String name;

        Mode(String name){
            this.name = name;
        }

        public String toString(){
            return name;
        }
    }


    @Override
    public void setInitialStateImp(AppState s){
        explorationCount = 0;
        sbwc = new ScreenBasedWeightCalculator(model, this);
        modeSwitch(Mode.ReadyForNextGoal);
    }

    private void rebuildModel(){
        buildModelAndUpdate();
        explorationCount = 0;
        sbwc = new ScreenBasedWeightCalculator(model, this);
    }


    public void useScreenBasedGoalSelection(){
        screenBasedGoalSelection = true;
    }

    public void useShallowTransitionBasedOptimization(){
        shallowTransitionBasedOptimization = true;
        executionFollower = new ExecutionFollower();
    }

    public void useExecutionTowardNewEvent(){
        executionTowardNewEvent = true;
        executionFollower = new ExecutionFollower2();
    }


    public void receiveResultImp(AppRequest request, AppResult result){
        if(shallowTransitionBasedOptimization || executionTowardNewEvent)
            executionFollower.add(result);

        //If application is terminated
        if(result.isStop()){
            rebuildModel();
            modeSwitch(Mode.ReadyForNextGoal);
            return;
        }

        //Mode Change
        switch(mode){
            case Drive:
                if(drivingFinished()){
                    coverageUpdate(result);
                    modeSwitch(Mode.Explore);
                }
                break;
            case Explore:
                coverageUpdate(result);
                if(explorationCount++ == explorationLimit){
                    rebuildModel();
                    modeSwitch(Mode.ReadyForNextGoal);
                }
                break;
            default:
                throw new RuntimeException("Something is Wrong");
        }
    }

    @Override
    protected boolean coverageUpdate(AppResult result){
        boolean newCoverage = super.coverageUpdate(result);
        if(newCoverage)
            explorationCount = 0;

        return newCoverage;
    }

    protected void selectGoal(){
        if(shallowTransitionBasedOptimization)
            executionFollower.reset();

        setDrivingGoal(pickGoal());
        System.out.println("Target :"  + drivingGoal.getId());
    }

    @Override
    public AppRequest getRequestImp(AppState s){

        switch(mode){
            case ReadyForNextGoal:
                modeSwitch(Mode.Drive);
                selectGoal();
                return createNewDriveRequest();

            case Drive:
                return createDriveRequest(s);

            case Explore:
                CSet<ICommand> enabledInputs = LearningUtil.getPalette(s);
                ICommand nextCommand = null;

                if(shallowTransitionBasedOptimization || executionTowardNewEvent){
                    Search:
                    for(ICommand candidateCommand:LearningUtil.getPalette(s)){
                        if(possiblyShallow(candidateCommand.getWidgetType()) && !executionFollower.getTabooSet().contains(candidateCommand)){
                            nextCommand = candidateCommand;
                            break Search;
                        }
                    }
                    if(nextCommand == null)
                        nextCommand = rand.pickWithTaboo(enabledInputs, executionFollower.getTabooSet());
                }

                if(nextCommand == null)
                    nextCommand = rand.pick(enabledInputs);

                return createRequest(nextCommand);
        }

        return null;
    }

    private boolean possiblyShallow(String viewType){
        return viewType.endsWith("TextView") || viewType.endsWith("DialogTitle") || viewType.endsWith("ImageView");
    }


    private State pickGoal(){
        if(screenBasedGoalSelection){
            return rand.weightedPick(model.getStatesYetToVisit(), sbwc);
        }
        else{
            return rand.pick(model.getStatesYetToVisit());
        }
    }

    private final RandomUtil rand = RandomUtil.getSharedInstance();

    private void modeSwitch(Mode mode){
        System.out.println("Learning Mode Switch:" + mode);
        this.mode = mode;

        if(mode == Mode.Explore && executionTowardNewEvent)
            executionFollower.reset();
    }

    @Override
    public boolean isWaitForNextSelection(){
        return mode == Mode.ReadyForNextGoal;
    }

    @Override
    public List<String> getOptionString(){
        return null;
    }

    @Override
    public void finish(){}
}




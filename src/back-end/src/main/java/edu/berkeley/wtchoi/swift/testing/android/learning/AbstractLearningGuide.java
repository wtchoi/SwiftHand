package edu.berkeley.wtchoi.swift.testing.android.learning;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;
import edu.berkeley.wtchoi.swift.testing.android.AppRequest;
import edu.berkeley.wtchoi.swift.testing.android.AppResult;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.swift.testing.android.AppTestingGuide;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.GraphModel;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.InconsistencyException;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.State;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.TreeModel;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.builder.BlueFringeBuilder;
import edu.berkeley.wtchoi.collection.*;

import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/24/13
 * Time: 1:34 AM
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
public abstract class AbstractLearningGuide implements AppTestingGuide {
    protected TreeModel tree;
    protected GraphModel model;

    protected State drivingGoal;
    protected List<ICommand> inputsToDrivingGoal;
    protected LinkedList<ICommand> remainedInpustToDrivingGoal = new LinkedList<ICommand>();
    protected int drivingGoalDepth;

    protected ProgramPointSet coverage = new ProgramPointSet();

    protected State currentTraceBase;
    protected List<ICommand> pathToCurrentTraceBase;
    protected LinkedList<CPair<ICommand, CSet<ICommand>>> currentTrace;
    protected Set<ICommand> observedCommand = new TreeSet<ICommand>();

    private int eventCounter;
    private boolean plotMergeSteps = false;

    private LearningTestingObserver observer;
    private Writer coverageLogWriter = new OutputStreamWriter(System.out);
    private Writer learningLogWriter = new OutputStreamWriter(System.out);

    public abstract void setInitialStateImp(AppState s);
    public abstract AppRequest getRequestImp(AppState s);
    public abstract void receiveResultImp(AppRequest request, AppResult result);
    public abstract boolean isWaitForNextSelection();


    //=============================
    //Methods from AppTestingGuide
    //=============================
    @Override
    public void setInitialState(AppState s){
        tree = new TreeModel(LearningUtil.getPalette(s));
        observer.setGuide(this);

        buildModel();

        currentTraceBase = model.getRoot();
        pathToCurrentTraceBase = currentTraceBase.path();
        currentTrace = new LinkedList<CPair<ICommand, CSet<ICommand>>>();

        setInitialStateImp(s);
    }


    final protected void buildModel(){
        long prev = System.currentTimeMillis();
        BlueFringeBuilder builder = new BlueFringeBuilder(tree);
        builder.setMergeOption(getBuilderMergeOption());
        if(plotMergeSteps) builder.plotMergeSteps();
        model = builder.build();
        long cur = System.currentTimeMillis();

        try{
            learningLogWriter.write(eventCounter + " ");
            learningLogWriter.write(model.getFrontiers().size() + " ");
            learningLogWriter.write(model.getStatesYetToVisit().size() + " ");
            learningLogWriter.write(String.valueOf(cur - prev));
            learningLogWriter.write("\n");
            learningLogWriter.flush();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    final protected void buildModelAndUpdate(){
        buildModel();
        currentTraceBase = model.getState(pathToCurrentTraceBase);
    }

    public void setDrivingGoal(State goal){
        setDrivingGoalWithPath(goal, goal.path());
    }

    public void setDrivingGoalWithPath(State goal, List<ICommand> path){
        pathToCurrentTraceBase.clear();

        inputsToDrivingGoal = path;
        remainedInpustToDrivingGoal.clear();
        remainedInpustToDrivingGoal.addAll(inputsToDrivingGoal);
        drivingGoalDepth = remainedInpustToDrivingGoal.size();

        currentTrace.clear();

        currentTraceBase = model.getRoot();

        if(drivingGoal != null)
            drivingGoal.setVeryInteresting(false);
        drivingGoal = goal;
        drivingGoal.setVeryInteresting(true);
    }

    //Assume : startState is final state of previous execution
    public void setDrivingGoal(final State start, final State goal){
        setDrivingGoalWithPath(start, goal, goal.pathFrom(start));
    }

    public void setDrivingGoalWithPath(State start, State goal, List<ICommand> path){
        for(Pair<ICommand, CSet<ICommand>> pair : currentTrace){
            pathToCurrentTraceBase.add(pair.fst);
        }

        State state2 = model.getState(pathToCurrentTraceBase);
        if(state2.id != start.id){
            model.getState(pathToCurrentTraceBase);
            throw new RuntimeException("Assumption Broken!");
        }

        inputsToDrivingGoal = path;
        remainedInpustToDrivingGoal.clear();
        remainedInpustToDrivingGoal.addAll(inputsToDrivingGoal);
        drivingGoalDepth = remainedInpustToDrivingGoal.size() + pathToCurrentTraceBase.size();

        currentTrace.clear();

        currentTraceBase = start;

        if(drivingGoal != null)
            drivingGoal.setVeryInteresting(false);
        drivingGoal = goal;
        drivingGoal.setVeryInteresting(true);
    }

    public void receiveResult(AppRequest request, AppResult result){
        try{
            updateTree(request, result);
        }
        catch(InconsistencyException ie){
            handleInconsistency(ie);
        }

        receiveResultImp(request, result);
    }

    protected void updateTree(AppRequest request, AppResult result) throws InconsistencyException {
        CList<ICommand> inputs = result.getExecutedCommands();
        List<AppState> outputStates = result.getResultingStateSequence();

        Iterator<ICommand> inputIterator = inputs.iterator();
        Iterator<AppState> stateIterator = outputStates.iterator();

        AppState lastState = null;
        while(inputIterator.hasNext()){
            ICommand cmd = inputIterator.next();
            observedCommand.add(cmd);

            lastState = stateIterator.next();
            CSet<ICommand> enabledInputs = LearningUtil.getPalette(lastState);
            currentTrace.add(new CPair<ICommand, CSet<ICommand>>(cmd, enabledInputs));
        }

        tree.addTrace(pathToCurrentTraceBase, currentTrace);
    }


    public ICommand getNextDriveStep(){
        return remainedInpustToDrivingGoal.removeFirst();
    }


    protected AppRequest createNewDriveRequest(){
        CVector<ICommand> inputSequence = new CVector<ICommand>();
        AppRequest request = new AppRequest(inputSequence);
        request.setRequestTrace();
        request.setRestartRequest();
        inputSequence.add(getNextDriveStep());

        return request;
    }

    protected AppRequest createDriveRequest(AppState s){
        //Command from the model and actual available input might be different in the detail
        //ex) hasFocus field of EnterCommand class objects
        //Therefore, we get an input from current status palette, instead of directly using
        CVector<ICommand> inputSequence = new CVector<ICommand>();
        AppRequest request = new AppRequest(inputSequence);
        request.setRequestTrace();

        //command from the model
        CSet<ICommand> actualPalette = LearningUtil.getPalette(s);
        ICommand cmd = getNextDriveStep();
        ICommand actualCmd = actualPalette.get(cmd);
        inputSequence.add(actualCmd);

        return request;
    }

    protected AppRequest createRequest(ICommand cmd){
        CVector<ICommand> inputSequence = new CVector<ICommand>();
        AppRequest request = new AppRequest(inputSequence);
        inputSequence.add(cmd);
        request.setRequestTrace();
        return request;
    }


    protected boolean drivingFinished(){
        if(drivingDiverge()){
            drivingDiverge();
            return true;
        }
        return (remainedInpustToDrivingGoal.size() == 0);
    }

    protected boolean drivingDiverge(){
        State cursor = getCurrentModelStateFromTrace();
        if(cursor.isYetToVisit) return remainedInpustToDrivingGoal.size() != 0;

        CSet<ICommand> lastOutput = currentTrace.getLast().snd;
        return (cursor.output.compareTo(lastOutput) != 0);
    }

    protected State getCurrentModelStateFromTrace(){
        State cursor = currentTraceBase; //model.getState(new LinkedList<ICommand>());
        for(CPair<ICommand, CSet<ICommand>> pair: currentTrace)
            cursor = cursor.getNext(pair.fst);

        return cursor;
    }


    @Override
    public AppRequest getRequest(AppState s){
        eventCounter++;
        return getRequestImp(s);
    }


    //PUBLIC : Interface for LearningObserver
    public void dumpInternal(String fileprefix){
        try{
            tree.writeTo(new FileWriter(fileprefix + ".tree.dot"), true);
            model.writeTo(new FileWriter(fileprefix + ".graph.dot"), false);
        }
        catch(Exception e){
            throw new RuntimeException("Something is wrong", e);
        }
    }

    //PUBLIC : Interface for LearningObserver
    public void forceDumpInternal(String fileprefix){
        try{
            tree.forceWriteTo(new FileWriter(fileprefix + ".tree.dot"), true);
            model.forceWriteTo(new FileWriter(fileprefix + ".graph.dot"), false);
        }
        catch(Exception e){
            throw new RuntimeException("Something is wrong", e);
        }
    }

    public void setObserver(LearningTestingObserver observer){
        this.observer = observer;
    }


    //PUBLIC : Interface for LearningObserver
    public void setLearningLog(String dir, String prefix){
        try{
            coverageLogWriter = new FileWriter(dir + "/" + prefix + "." + "learning.coverage", false);
            learningLogWriter = new FileWriter(dir + "/" + prefix + "." + "learning", false);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    protected boolean coverageUpdate(AppResult result){
        final ProgramPointSet newlycovered = new ProgramPointSet();
        result.getCoverage().foreach(new ProgramPointSet.ProgramPointVisitor() {
            @Override
            public void visit(int bid, short fid) {
                if(!coverage.contains(bid, fid))
                    newlycovered.add(bid, fid);
            }
        });

        coverage.addAll(result.getCoverage());

        if(newlycovered.size() != 0){
            logNewCoverage(newlycovered);
            return true;
        }
        return false;
    }


    private void logNewCoverage(ProgramPointSet newlycovered){
        try{
            coverageLogWriter.write(eventCounter + " ");
            coverageLogWriter.write(drivingGoalDepth + " ");
            coverageLogWriter.write(LearningUtil.stringOfPathPart(currentTrace) + " ");
            coverageLogWriter.write(LearningUtil.stringOfProgramPointSet(newlycovered));
            coverageLogWriter.write("\n");
            coverageLogWriter.flush();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }


    protected void handleInconsistency(InconsistencyException ie){
        StringBuilder builder = new StringBuilder();
        builder.append("Being inconsistent for 1 times?\n");
        builder.append("path:\t" + LearningUtil.stringOfPath(ie.mPath) + "\n");
        builder.append("model:\t" + LearningUtil.stringOfPalette(ie.mModelPalette) + "\n");
        builder.append("observed:\t" + LearningUtil.stringOfPalette(ie.mObservedPalette) + "\n");
        throw new RuntimeException(builder.toString(), ie);
    }

    protected BlueFringeBuilder.MergeOption getBuilderMergeOption(){
        return BlueFringeBuilder.MergeOption.ToFittest;
    }

    public void plotMergeSteps(){
        plotMergeSteps = true;
    }
}

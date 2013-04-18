package edu.berkeley.wtchoi.swift.testing.android.learning;

import edu.berkeley.wtchoi.swift.driver.DragCommand;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.PushCommand;
import edu.berkeley.wtchoi.swift.testing.android.AppRequest;
import edu.berkeley.wtchoi.swift.testing.android.AppResult;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.GraphModel;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.State;
import edu.berkeley.wtchoi.swift.util.Predicate;
import edu.berkeley.wtchoi.swift.util.RandomUtil;
import edu.berkeley.wtchoi.swift.util.statistics.SimpleTracker;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/24/13
 * Time: 10:07 PM
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
public class ActiveLearningGuide extends AbstractLearningGuide{

    public static enum MergeOption{
        ToParent,
        ToAncestor,
        ToNearest,
        Hybrid,
        NoOption;
    }

    public static enum DriveOption{
        UseUnrealizedPath,
        NoOption;
    }

    private int traceLimit = 50;
    private int depthLimit = 3;
    State currentModelState;
    Mode mode;

    DriveOption driveOption = DriveOption.NoOption;
    MergeOption mergeOption = MergeOption.NoOption;
    RandomUtil randomUtil = RandomUtil.getSharedInstance();

    Predicate<State> frontierConstraint = new Predicate<State>() {
        @Override
        public boolean check(State state) {
            return state.isFrontier();
        }
    };

    SimpleTracker learningTracker = new SimpleTracker();

    public void setDriveOption(DriveOption opt){
        driveOption = opt;
    }

    public void setMergeOption(MergeOption opt){
        mergeOption = opt;
    }



    enum Mode{
        Drive,
        Restart,
        Refine
    }

    private void rebuildModel(){
        buildModelAndUpdate();
        setCurrentModelState(getCurrentModelStateFromTrace());
    }

    //Assume : no divergence
    //Assume : only the last one transition can be "new" w.r.t model.
    private void updateModel(AppRequest request, AppResult result){
        Iterator<ICommand> cmdIter = result.getExecutedCommands().iterator();
        Iterator<AppState> stateIter = result.getResultingStateSequence().iterator();

        State cursor = currentModelState;
        State treeCursor = tree.getState(result.getStaringState().getHistory());

        while(cmdIter.hasNext()){
            ICommand cmd = cmdIter.next();
            AppState state = stateIter.next();

            State next = cursor.getNext(cmd);
            treeCursor = treeCursor.getNext(cmd);
            ((GraphModel.GraphState) next).addSource(treeCursor);

            if(next.isYetToVisit){
                CSet<ICommand> enabledInputs = LearningUtil.getPalette(state);
                Set<State> mergeCandidates = model.getStatesWithScreen(enabledInputs);

                if(!mergeCandidates.isEmpty()){
                    switch(mergeOption){
                        case ToParent:
                            cursor = doMergeToParent(next, cursor, mergeCandidates, enabledInputs);
                            break;
                        case ToAncestor:
                            cursor = doMergeToAncestor(next, mergeCandidates,  enabledInputs);
                            break;
                        case ToNearest:
                            cursor = doMergeToNearest(next, mergeCandidates, enabledInputs);
                            break;
                        case Hybrid:
                            cursor = (randomUtil.nextBoolean())
                                    ? doMergeToAncestor(next, mergeCandidates, enabledInputs)
                                    : doMergeToNearest(next, mergeCandidates, enabledInputs);
                            break;
                        default:
                            cursor = doMergeDefault(next, mergeCandidates);
                    }
                    continue;
                }

                model.forceVisitState(next, enabledInputs);
            }

            cursor = next;
        }
        setCurrentModelState(cursor);
    }

    private State doMergeToNearest(State source, Set<State> candidates, CSet<ICommand> enabledInputs){
        LinkedList<State> queue = new LinkedList<State>();
        TreeSet<State> considered = new TreeSet<State>();
        queue.add(source);
        considered.add(source);

        while(!queue.isEmpty()){
            State state = queue.removeFirst();
            for(Pair<State, ICommand> pair: state.getParents()){
                State parent = pair.getFirst();

                if(considered.contains(parent)) continue;

                if(parent.output.equalsTo(enabledInputs)){
                    model.merge(parent, source);
                    return parent;
                }

                considered.add(parent);
                queue.add(parent);
            }

            if(state.output == null) continue;
            for(State child:state.getChildren()){
                if(considered.contains(child)) continue;
                if(child.output == null) continue;

                if(child.output.equalsTo(enabledInputs)){
                    model.merge(child, source);
                    return child;
                }

                considered.add(child);
                queue.add(child);
            }
        }

        return doMergeDefault(source, candidates);
    }


    private State doMergeToAncestor(State source, Set<State> candidates, CSet<ICommand> enabledInputs){
        LinkedList<State> queue = new LinkedList<State>();
        TreeSet<State> considered = new TreeSet<State>();
        queue.add(source);
        considered.add(source);

        while(!queue.isEmpty()){
            State state = queue.removeFirst();
            for(Pair<State, ICommand> pair: state.getParents()){
                State parent = pair.getFirst();

                if(considered.contains(parent)) continue;

                if(parent.output.equalsTo(enabledInputs)){
                    model.merge(parent, source);
                    return parent;
                }

                considered.add(parent);
                queue.add(parent);
            }
        }

        return doMergeDefault(source, candidates);
    }


    private State doMergeToParent(State source, State parent, Set<State> candidates, CSet<ICommand> enabledInputs){
        if(parent.output.equalsTo(enabledInputs)){
            model.merge(parent, source);
            return parent;
        }

        return doMergeDefault(source, candidates);
    }

    private State doMergeDefault(State source, Set<State> candidates){
        State mergeTarget = randomUtil.pick(candidates);
        model.merge(mergeTarget, source);
        return mergeTarget;
    }

    public void setInitialStateImp(AppState s){
        switchMode(Mode.Restart);
        learningTracker.start();
    }


    public AppRequest getRequestImp(AppState s){
        switch(mode){
            case Refine :
            case Drive  :
                return createDriveRequest(s);
            case Restart:
                setCurrentModelState(model.getRoot());

                if(!model.isClosed()){ //usual drive
                    switchMode(Mode.Drive);
                    State frontier = pickFrontierFromRoot();
                    setDrivingGoal(pickYetToVisit(frontier));
                }
                else{ //refinement
                    model.isClosed();
                    System.out.println("Refinement!");
                    switchMode(Mode.Refine);
                    setNextRefinementGoalFromRoot();
                }
                return createNewDriveRequest();
            default:
                throw new RuntimeException("Unreachable Reached!");
        }
    }


    //Assume lstar is already updated
    public void receiveResultImp(AppRequest request, AppResult result){
        if(result.isStop()){
            if(drivingDiverge())
                rebuildModel();
            else
                updateModel(request,result);

            switchMode(Mode.Restart);
            learningTracker.stop();
            learningTracker.start();
        }
        else if(drivingFinished()){
            coverageUpdate(result);

            if(drivingDiverge())
                rebuildModel();
            else
                updateModel(request, result);

            if(resetRequired()){
                switchMode(Mode.Restart);
            }
            else{
                if(!model.isClosed()){
                    switchMode(Mode.Drive);
                    setNextDrivingGoal();
                }
                else{
                    model.isClosed();
                    System.out.println("Refinement!");
                    switchMode(Mode.Refine);
                    setNextRefinementGoal();
                }
            }
            learningTracker.stop();
            learningTracker.start();
        }
        else{
            for(ICommand cmd : result.getExecutedCommands())
                setCurrentModelState(currentModelState.getNext(cmd));
        }
    }

    private void setNextRefinementGoalFromRoot(){
        Goal goal = findShortestUnrealizedSubpath(model.getRoot(), null, -1);
        setDrivingGoalWithPath(goal.state, goal.path);
    }



    private void setNextRefinementGoal(){
        Goal goal = findShortestUnrealizedSubpath(currentModelState, null, -1);
        setDrivingGoalWithPath(currentModelState, goal.state, goal.path);
    }


    private Goal findShortestUnrealizedSubpath(State start, Predicate<State> stateConstraint, int depthLimit){
        LinkedList<State> queue = new LinkedList<State>();
        LinkedList<Collection<State>> witnessesQueue = new LinkedList<Collection<State>>();
        LinkedList<LinkedList<ICommand>> pathQueue = new LinkedList<LinkedList<ICommand>>();

        queue.add(start);
        Vector<State> firstWitnesses = new Vector<State>();
        firstWitnesses.addAll(((GraphModel.GraphState) start).sourceStates);
        witnessesQueue.add(firstWitnesses);
        pathQueue.add(new LinkedList<ICommand>());

        while(!queue.isEmpty()){
            State state = queue.removeFirst();
            Collection<State> witnesses = witnessesQueue.removeFirst();
            LinkedList<ICommand> currentPath = pathQueue.removeFirst();
            if(currentPath.size() == depthLimit) continue;

            for(ICommand cmd : state.output){
                if(blockCondition(currentPath, cmd)) continue;

                State nextState = state.getNext(cmd);
                if(nextState.isYetToVisit()) continue;

                Collection<State> nextWitnesses = getNextWitnesses(nextState, witnesses, cmd);
                LinkedList<ICommand> nextPath = new LinkedList<ICommand>();
                nextPath.addAll(currentPath);
                nextPath.add(cmd);

                if(nextWitnesses.isEmpty()){
                    if(stateConstraint == null || stateConstraint.check(nextState)){
                        return new Goal(nextState, nextPath);
                    }
                    if(!nextState.isReachableTo(frontierConstraint))
                        continue;
                }

                queue.add(nextState);
                witnessesQueue.add(nextWitnesses);
                pathQueue.add(nextPath);
            }
        }

        return null;
    }

    private boolean blockCondition(List<ICommand> prefix, ICommand cmd){
        if(prefix.isEmpty()) return false;
        ICommand lastCommand = prefix.get(prefix.size() - 1);
        if(lastCommand.equalsTo(PushCommand.getMenu())){
            if(cmd.equalsTo(PushCommand.getMenu())) return true;
            if(cmd.equalsTo(PushCommand.getBack())) return true;
        }

        if(lastCommand instanceof DragCommand && cmd instanceof  DragCommand){
            if(((DragCommand) cmd).isDualTo((DragCommand) lastCommand)) return true;
        }
        return false;
    }

    private Collection<State> getNextWitnesses(State targetState, Collection<State> witnesses, ICommand cmd){
        Vector<State> nextWitnesses = new Vector<State>();
        for(State witness:witnesses){
            if(witness.hasNext(cmd)){
                State nextWitness = witness.getNext(cmd);
                if(nextWitness.isYetToVisit) continue;

                if(targetState.output.compareTo(nextWitness.output) != 0)
                    throw new RuntimeException("Inconsistency should be detected before!");

                nextWitnesses.add(nextWitness);
            }
        }
        return nextWitnesses;
    }


    private void setNextDrivingGoal(){
        if(currentModelState.isFrontier()){
            State newGoal =  pickYetToVisit(currentModelState);
            setDrivingGoal(currentModelState, newGoal);
        }
        else{
            Goal frontier =  pickFrontier();
            if(frontier == null){
                switchMode(Mode.Restart);
                return;
            }
            State newGoal = pickYetToVisit(frontier.state);
            List<ICommand> path = frontier.path;
            path.addAll(newGoal.pathFrom(frontier.state));
            setDrivingGoalWithPath(currentModelState, newGoal, path);
        }

    }


    private boolean resetRequired(){
        return currentTrace.size() > traceLimit;
    }

    private State pickYetToVisit(State frontier){
        TreeSet<State> candidates = new TreeSet<State>();
        for(State child : frontier.getChildren()){
            if(child.isYetToVisit)
                candidates.add(child);
        }

        if(candidates.isEmpty()){
            throw new RuntimeException("How could frontier have no candidate?");
        }

        return randomUtil.pick(candidates);
    }

    private Goal pickFrontier(){
        switch(driveOption){
            case UseUnrealizedPath:
                Goal goal2 = pickNearestFrontier();
                if(goal2 == null) return null;

                Goal goal1 = pickUnrealizedPathToFrontier(goal2.path.size() * 2);

                if(goal1 == null) return goal2;
                return goal1;

            default:
                return pickNearestFrontier();
        }
    }


    private Goal pickUnrealizedPathToFrontier(int depthLimit){
        return findShortestUnrealizedSubpath(currentModelState, frontierConstraint, depthLimit);
    }

    private Goal pickNearestFrontier(){
        TreeSet<State> candidates = new TreeSet<State>();
        TreeSet<State> consideredStates = new TreeSet<State>();
        LinkedList<Pair<State, Integer>> queue = new LinkedList<Pair<State, Integer>>();
        queue.add(new Pair<State, Integer>(currentModelState, depthLimit));
        consideredStates.add(currentModelState);

        Pair<State, Integer> cur;
        while(!queue.isEmpty()){
            cur = queue.removeFirst();
            State state = cur.getFirst();
            Integer dlimit = cur.getSecond();

            if(state.isFrontier()) candidates.add(state);
            if(dlimit > 0){
                Set<State> children = state.getChildren();
                for(State child : children){
                    if(!consideredStates.contains(child) && !state.isYetToVisit){
                        queue.add(new Pair<State, Integer>(child, dlimit - 1));
                        consideredStates.add(child);
                    }
                }
            }
        }

        if(candidates.size() == 0) return null;
        State s =  randomUtil.pick(candidates);

        return new Goal(s, s.pathFrom(currentModelState));
    }

    private State pickFrontierFromRoot(){
        return randomUtil.pick(model.getFrontiers());
    }


    @Override
    public boolean isWaitForNextSelection(){
        return mode == Mode.Restart;
    }


    private void switchMode(Mode m){
        mode = m;
    }

    private void setCurrentModelState(State state){
        if(currentModelState != null)
            currentModelState.setInteresting(false);
        currentModelState = state;
        currentModelState.setInteresting(true);
    }

    public List<String> getOptionString(){
        List<String> str = new Vector<String>();
        str.add("M:" + mergeOption.toString() + ", D:" + driveOption.toString());
        str.add("mean time to learning : " + learningTracker.average());
        return str;
    }

    @Override
    public void finish(){}{
        learningTracker.stop();
    }
}


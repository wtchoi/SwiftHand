package edu.berkeley.wtchoi.swift.testing.android.lstar;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.android.lstar.ctree.CState;
import edu.berkeley.wtchoi.swift.testing.android.lstar.ctree.CTree;
import edu.berkeley.wtchoi.swift.util.RandomUtil;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.CVector;
import edu.berkeley.wtchoi.collection.ExtendedComparable;

import java.util.Deque;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:59 PM
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

public class TreeLearner{
    private CTree ctree;
    //private CSet<ICommand> defaultPalette;
    private int defaultDegree = 1;

    boolean randomOrderObservation = false;
    boolean usePruning = false;

    private CSet<CState> uniqueStates; //S
    private CSet<CState> frontierStates;  //SI

    private CSet<CState> uniquesToBeTested; //for implementation
    private CSet<CState> frontiersToBeTested; //for implementation
    //INVARIANT : state in stateToBeTested <==> state has remainedObservations

    private TreeMap<CState,CSet<CList<ICommand>>> remainedObservations;
    private TreeMap<CState,CSet<CList<ICommand>>> fetchedObservations;

    private CSet<CState> pendingStates;
    private TreeMap<CState, Integer> observationDegree;
    private final int resumeThreshold = 5;

    RandomUtil random = RandomUtil.getSharedInstance();

    private TreeMap<CSet<ICommand>, CSet<CList<ICommand>>> suffixes;

    CState stateOnResume;
    CState stateOnCompare;


    public TreeLearner(CSet<ICommand> initialPalette) {
        ctree = new CTree(initialPalette);//,defaultPalette);
        //ctree.startViewer();

        uniqueStates = new CSet<CState>();
        frontierStates = new CSet<CState>();

        uniquesToBeTested = new CSet<CState>();
        frontiersToBeTested = new CSet<CState>();
        remainedObservations = new TreeMap<CState, CSet<CList<ICommand>>>();
        fetchedObservations = new TreeMap<CState, CSet<CList<ICommand>>>();

        pendingStates = new CSet<CState>();
        observationDegree = new TreeMap<CState, Integer>();

        suffixes = new TreeMap<CSet<ICommand>, CSet<CList<ICommand>>>();

        CState initState = ctree.getInitState();
        addToFrontier(initState);
    }

    private void addToFrontier(CState s){
        frontierStates.add(s);
        frontiersToBeTested.add(s);

        remainedObservations.put(s, new CSet<CList<ICommand>>());
        fetchedObservations.put(s, new CSet<CList<ICommand>>());

        CSet<ICommand> palette = ctree.getPalette(s);
        remainedObservations.get(s).add(ExpandToCommand.getVector(1));
        if(suffixes.containsKey(palette))
            remainedObservations.get(s).addAll(suffixes.get(palette));
        observationDegree.put(s, defaultDegree);

        s.setColor("blue");
    }

    private void promoteToUnique(CState state){
        frontierStates.remove(state);
        //observationDegree.remove(state);
        uniqueStates.add(state);

        CSet<ICommand> palette = ctree.getPalette(state);
        if(!suffixes.containsKey(palette))
            suffixes.put(palette, new CSet<CList<ICommand>>());

        for(ICommand cmd:palette){
            CState f = ctree.getState(state, cmd);
            if(uniqueStates.contains(f)) continue;
            if(f.isStopNode()) continue;
            addToFrontier(f);
        }

        state.setColor("black");
    }

    public ExploreRequest<ICommand> getRequest(CList<ICommand> machineState){
        ExploreRequest<ICommand> request = getRequestImp(machineState);
        updateView();
        return request;
    }

    static int count = 0;
    public ExploreRequest<ICommand> getRequestImp(CList<ICommand> machineStatePrefix) {
        System.out.println("search round : "+ ++count);
        if(count == 109){
            int x = 1;
        }


        outerLoop:
        while(true){
            CState machineState = ctree.getState(machineStatePrefix);

            uniqueLoop:
            while(!uniquesToBeTested.isEmpty()){
                CState state = pickState(uniquesToBeTested, machineState);
                CList<ICommand> suffix = pollObservation(state);

                //if(remainedObservations.get(state).isEmpty() ){
                if(checkFullyObserved(state)){
                    uniquesToBeTested.remove(state);
                }

                if(suffix == null){
                    continue uniqueLoop;
                }

                return buildRequest(machineState, state, suffix);
            }

            frontierLoop:
            while(!frontiersToBeTested.isEmpty()){
                CState state = pickState(frontiersToBeTested, machineState);
                CList<ICommand> suffix = pollObservation(state);

                if(checkFullyObserved(state))
                //if(remainedObservations.get(state).isEmpty())
                    frontiersToBeTested.remove(state);

                if(suffix == null && !frontiersToBeTested.contains(state)){
                    if(closeState(state))
                        continue frontierLoop;
                    else
                        continue outerLoop;
                }

                return buildRequest(machineState, state, suffix);
            }
            if(!tryResume()) return null;
            updateView();
            //return getRequestImp(machineStatePrefix);
            //return null;
        }
    }

    private boolean tryResume(){
        if(stateOnResume != null) return false;

        CState candidate = null;
        int degree = defaultDegree;
        int candidateDegree = defaultDegree;

        int priority = 1000000000;
        for(CState state : pendingStates){
            degree = observationDegree.get(state);
            int p = degree * degree * resumeThreshold;
            if(p < priority){
                candidate = state;
                priority = p;
                candidateDegree = degree;
            }
        }
        if(candidate == null) return false;

        observationDegree.put(candidate, ++candidateDegree);

        frontiersToBeTested.add(candidate);
        remainedObservations.get(candidate).add(ExpandToCommand.getVector(candidateDegree));

        CState mergeTarget = candidate.getMergeTo();
        uniquesToBeTested.add(mergeTarget);
        remainedObservations.get(mergeTarget).add(ExpandToCommand.getVector(candidateDegree));
        observationDegree.put(mergeTarget, candidateDegree);

        stateOnResume = candidate;
        stateOnCompare = candidate.getMergeTo();
        pendingStates.remove(candidate);
        candidate.split();

        return true;
    }

    private CList<ICommand> pollObservation(CState state){
        CList<ICommand> suffix;
        CSet<CList<ICommand>> observations = remainedObservations.get(state);
        CSet<CList<ICommand>> candidates = new CSet<CList<ICommand>>();

        while((suffix = observations.pollFirst()) != null){
            if(tryExpand(state, suffix)) continue;
            if(!ctree.checkPossible(state, suffix)) continue;
            if(ctree.visited(state, suffix))
                continue;

            if(!randomOrderObservation) return suffix;
            candidates.add(suffix);
        }

        if(candidates.size() == 0) return null;

        suffix = random.pick(candidates);
        candidates.remove(suffix);
        observations.addAll(candidates);
        return suffix;

    }

    private boolean tryExpand(CState state, CList<ICommand> suffix){
        if(suffix.size() == 0) return false;

        ICommand lastCmd = suffix.get(0);
        if(!(lastCmd instanceof ExpandToCommand)) return false;

        ExpandToCommand expander = (ExpandToCommand) lastCmd;
        suffix.remove(0);

        CSet<ICommand> palette = ctree.getPalette(ctree.getState(state, suffix));
        remainedObservations.get(state).addAll((expander.expand(suffix, palette)));
        return true;
    }


    private CState pickState(CSet<CState> set, CState machineState){
        if(set.contains(machineState)) return machineState;
        for(CState s: set){
            if(machineState.isPrefixOf(s)) return s;
        }

        if(randomOrderObservation) return random.weightedPick(set, weightCalculator);
        else return set.first();
    }


    private RandomUtil.WeighCalculator<CState> weightCalculator = new RandomUtil.WeighCalculator<CState>(){
        @Override
        public double weight(CState state){
            double w = 1.0;
            double e = ctree.getAverageChildren();
            //for(int i = 0 ; i<state.getDepth(); i++){
            //    w *= e;
            //}
            return 1.0 / (Math.log(e) * ctree.depth() + 1.0);
        }
    };


    private ExploreRequest<ICommand> buildRequest(CState machineState, CState sut, CList<ICommand> suffix){
        fetchedObservations.get(sut).add(suffix);

        CList<ICommand> question = new CVector<ICommand>();
        question.addAll(sut.getInput());
        question.addAll(suffix);

        if(!machineState.isPrefixOf(question)){
            return new ExploreRequest<ICommand>(false, question, sut.getInput(), suffix);
        }
        machineState.removePrefixFrom(question);
        return new ExploreRequest<ICommand>(true, question, sut.getInput(), suffix);
    }


    private boolean closeState(CState state){
        if(state.isStopNode()){
            frontierStates.remove(state);
            updateView();
            return true;
        }

        if(stateOnResume != null && stateOnResume.compareTo(state) == 0){
            boolean result;
            if(checkObservationalEquivalence(stateOnCompare, state)){
                pending(state, stateOnCompare);
                result = true;
            }
            else{
                CSet<CList<ICommand>> refutation = getRefutation();
                suffixes.get(ctree.getPalette(state)).addAll(refutation);
                for(CState t : uniqueStates)    remainedObservations.get(t).addAll(refutation);
                for(CState t : frontierStates)  remainedObservations.get(t).addAll(refutation);
                for(CState t: pendingStates)    remainedObservations.get(t).addAll(refutation);
                uniquesToBeTested.addAll(uniqueStates);
                frontiersToBeTested.addAll(frontierStates);
                frontierStates.add(state);
                result = false;
                //promoteToUnique(state);
            }

            stateOnResume = null;
            stateOnCompare = null;
            return result;
        }

        for(CState ustate: uniqueStates){
            if(checkObservationalEquivalence(ustate, state)){
                pending(state, ustate);
                return true;
            }
        }
        promoteToUnique(state);
        return true;
    }

    private void pending(CState state, CState ustate){
        pendingStates.add(state);
        state.mergeTo(ustate);
    }

    private boolean checkObservationalEquivalence(CState uState, CState fState){
        int degree = observationDegree.get(fState);
        if(!checkObservationalEquivalence(uState, fState, degree)) return false;
        for(CList<ICommand> suffix : suffixes.get(ctree.getPalette(fState))){
            boolean f1 = ctree.checkPossible(uState, suffix);
            boolean f2 = ctree.checkPossible(fState, suffix);
            if(f1 != f2) return false;
            CList<Observation> o1 = ctree.getTransition(uState, suffix);
            CList<Observation> o2 = ctree.getTransition(fState, suffix);
            if(o1.compareTo(o2) != 0) return false;
        }
        return true;
    }

    private Deque<ICommand> counterExample;

    private boolean checkObservationalEquivalence(CState uniqueState, CState frontierState, int degree){
        if(!checkFullyObserved(uniqueState) || !checkFullyObserved(frontierState)){
            boolean xx = checkFullyObserved(uniqueState);
            boolean yy = checkFullyObserved(frontierState);
            System.out.println(xx);
            System.out.println(yy);
            throw new RuntimeException("Something is Wrong!");
        }


        counterExample = new LinkedList<ICommand>();

        CSet<ICommand> p1 = ctree.getPalette(uniqueState);
        CSet<ICommand> p2 = ctree.getPalette(frontierState);
        if(p1.compareTo(p2) != 0) return false;

        if(!checkObservationalEquivalenceImp(uniqueState, p1, frontierState, p2, degree)) return false;
        return true;
    }

    private boolean checkObservationalEquivalenceImp(CState s1, CSet<ICommand> p1, CState s2, CSet<ICommand> p2, int degree){

        if(s1.isStopNode() && s2.isStopNode()) return true;
        if(s1.compareTo(s2) == 0) return true;

        System.out.println("(" + s1.toString() + "," + s2.toString() + ")"+ degree);
        if(degree == 0) return true;

        //##DEBUG HOOK
        if(degree == 2) degree = degree;

        CSet<ICommand> commands = new CSet<ICommand>();
        commands.addAll(p1);
        //commands.addAll(defaultPalette);

        //First check whether next states has same view or not
        for(ICommand cmd: commands){
            counterExample.push(cmd);
            Observation o1 = ctree.getTransition(s1,cmd);
            Observation o2 = ctree.getTransition(s2,cmd);
            if(!o1.equalsTo(o2)) return false;
            counterExample.pop();
        }

        //Then, check whether two state has same observational behavior
        if(degree > 1){
            for(ICommand cmd : commands){
                counterExample.push(cmd);
                CState ch1 = ctree.getState(s1, cmd);
                CState ch2 = ctree.getState(s2, cmd);
                //Skip if transitions for both state are self loop
                if(!(isSelfLoop(s1,ch1) && isSelfLoop(s2,ch2))){
                    CSet<ICommand> pch1 = ctree.getPalette(ch1);
                    CSet<ICommand> pch2 = ctree.getPalette(ch2);
                    if(! checkObservationalEquivalenceImp(ch1, pch1, ch2, pch2, degree - 1)) return false;
                }
                counterExample.pop();
            }
        }
        return true;
    }

    private boolean isSelfLoop(CState s1, CState s2){
        return s1.compareTo(s2) == 0;
    }

    private CSet<CList<ICommand>> getRefutation(){
        CSet<CList<ICommand>> refutationSet = new CSet<CList<ICommand>>();
        for(ICommand cmd : counterExample){
            for(CList<ICommand> cmdString : refutationSet){
                cmdString.add(cmd);
            }
            CList<ICommand> temp = new CVector<ICommand>();
            temp.add(cmd);
            refutationSet.add(temp);
        }
        return refutationSet;
    }

    public void learn(ExploreResult<ICommand,Observation> result) {
        learnImp(result);
    }

    private void learnImp(ExploreResult<ICommand,Observation> result) {
        CList<ICommand> equalInput = null;
        CState state;

        CState startingState = ctree.getState(result.startingState);
        ctree.addPath(startingState, result.input, result.output);

        if(usePruning)
            equalInput = ctree.tryPruning(startingState, result.input);

        state = ctree.getState(startingState, result.input);

        if(result.query != null){
            CState sut = ctree.getState(result.query.sut);
            fetchedObservations.get(sut).remove(result.query.suffix);
            updateView();

            if(frontierStates.contains(sut) && checkFullyObserved(sut))
                closeState(sut);
        }

        if(usePruning){
            if(equalInput == null){
                System.out.println("reached state : " + state);
            }
        }

        updateView();
    }

    public CList<ICommand> recommend(CList<ICommand> statePrefix){
        return ctree.recommendNextRandomly(ctree.getState(statePrefix));
    }


    private boolean checkFullyObserved(CState s){
        return remainedObservations.get(s).isEmpty() && fetchedObservations.get(s).isEmpty();
    }

    public void updateView(){
        ctree.updateView();
    }

    public void dumpToDot(String filename){
        ctree.dumpToDot(filename);
    }

    public long getSeed(){
        return random.getSeed();
    }

    public static class ExploreResult<I extends ExtendedComparable<I>, O extends ExtendedComparable<O>> {
        public CList<I> startingState;
        public CList<I> input;
        public CList<O> output;
        public ExploreRequest<I> query;

        public ExploreResult(CList<I> startingInput, CList<I> input, CList<O> output){
            this.startingState = startingInput;
            this.input  = input;
            this.output = output;
            this.query  = null;
        }
    }

    public static class ExploreRequest<I extends ExtendedComparable<I>> {
        public boolean fromCurrentState;
        public CList<I> input;

        public CList<I> sut;
        public CList<I> suffix;

        public ExploreRequest(boolean flag, CList<I> r, CList<I> sut, CList<I> suffix){
            fromCurrentState = flag;
            input = r;

            this.sut = sut;
            this.suffix = suffix;
        }
    }
}


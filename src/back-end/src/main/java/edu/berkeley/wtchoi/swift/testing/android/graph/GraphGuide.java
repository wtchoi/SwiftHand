package edu.berkeley.wtchoi.swift.testing.android.graph;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;
import edu.berkeley.wtchoi.swift.testing.android.AppRequest;
import edu.berkeley.wtchoi.swift.testing.android.AppResult;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.swift.testing.android.AppTestingGuide;
import edu.berkeley.wtchoi.swift.testing.android.graph.graph.Graph;
import edu.berkeley.wtchoi.swift.testing.android.graph.graph.GraphState;
import edu.berkeley.wtchoi.swift.testing.android.graph.tree.Tree;
import edu.berkeley.wtchoi.swift.testing.android.graph.tree.TreeState;
import edu.berkeley.wtchoi.swift.util.RandomUtil;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CVector;

import java.io.FileWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/8/13
 * Time: 8:12 PM
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
public class GraphGuide implements AppTestingGuide {

    private Graph graph;
    private Tree tree;
    private GraphTestingObserver observer;
    private boolean strictMode = true;

    public void setObserver(GraphTestingObserver obsv){
        observer = obsv;
    }

    public void setInitialState(AppState s){
        tree = new Tree(s);
        graph = new Graph(tree);
        observer.setGuide(this);
    }

    public AppRequest getRequest(AppState currentAppState){
        if(!graph.hasFrontier()) refine();

        if(strictMode) return getRequestStrict(currentAppState);
        else return getRequestNormal(currentAppState);
    }

    private AppRequest getRequestStrict(AppState currentAppState){
        GraphState currentState = graph.getCorrespondingGraphState(tree.getCorrespondingTreeState(currentAppState));

        if(!currentAppState.isStop()){
            CList<ICommand> curHistory = new CVector<ICommand>();
            curHistory.addAll(currentAppState.getHistory());

            CList<ICommand> expectedHistory = new CVector<ICommand>();
            TreeState expectedState = currentState.getRepresentativeState();
            try{
                expectedHistory.addAll(expectedState.getPath());
            }
            catch(Exception e){
                int x = 1;
            }

            if(curHistory.equalsTo(expectedHistory)){
                for(ICommand cmd: currentState.palette){
                    GraphState candidate = currentState.getSuccessor(cmd);
                    if(!candidate.isVisited()) return buildRequest(candidate, currentState, false);
                }
            }
        }
        return buildRequest(pickRandom(graph.getFrontiers()), currentState, true);
    }

    private AppRequest getRequestNormal(AppState currentAppState){
        GraphState currentState = graph.getCorrespondingGraphState(tree.getCorrespondingTreeState(currentAppState));
        Set<GraphState> graphStateSet = graph.getFrontiers();

        GraphState selected = null;

        if(checkDfsMode()){
            selected = pickDFS(graphStateSet, currentState);
            if(selected != null) return buildRequest(selected, currentState, false);
        }

        selected = pickRandom(graphStateSet);
        return buildRequest(selected, currentState, true);
    }

    private boolean checkDfsMode(){
        return graphDfsCount < graphDfsLimit;
    }

    private int graphDfsLimit = 3;
    private int graphDfsCount = 0;

    private GraphState pickDFS(Set<GraphState> graphStateSet, GraphState currentState){
        for(GraphState candidate:graphStateSet){
            if(graph.checkTransitivity(currentState, candidate)){
                graphDfsCount++;
                return candidate;
            }
        }
        return null;
    }

    RandomUtil randomUtil = new RandomUtil();

    private GraphState pickRandom(Set<GraphState> graphStateSet){
        graphDfsCount = 0;

        final TreeMap<Integer, Integer> perDepthCount = new TreeMap<Integer, Integer>();

        for(GraphState state:graph.getFrontiers()){
            if(!perDepthCount.containsKey(state.depth)) perDepthCount.put(state.depth, 1);
            else perDepthCount.put(state.depth, perDepthCount.get(state.depth) + 1);
        }


        RandomUtil.WeighCalculator<GraphState> calc = new RandomUtil.WeighCalculator<GraphState>() {
            @Override
            public double weight(GraphState elt) {
                return 1.0  / ((double)((perDepthCount.get(elt.depth)) * elt.depth * elt.depth));
            }
        };

        return randomUtil.weightedPick(graphStateSet, calc);
        //return randomUtil.pick(graphStateSet);
    }


    private void refine(){
        throw new RuntimeException("Not Implemented Yet!");
        //TODO
    }



    private AppRequest buildRequest(final GraphState targetGraphState, final GraphState currentGraphState, boolean reset){

        CList<ICommand> inputSequence = new CVector<ICommand>();
        AppRequest request;

        if(!reset){
            boolean hasTransition = graph.getTransition(currentGraphState, targetGraphState, inputSequence);
            if(hasTransition){
                request = new AppRequest(inputSequence);
                request.setRequestTrace();
                return request;
            }
        }

        graph.getTransition(graph.getInitState(), targetGraphState, inputSequence);
        request = new AppRequest(inputSequence);
        request.setRestartRequest();
        request.setRequestTrace();
        return request;
    }


    public void receiveResult(AppRequest r, AppResult rr){
        AppState staringAppState = rr.getStaringState();
        CList<ICommand> inputSequence = rr.getExecutedCommands();
        List<CompressedLog> observationSequence = rr.getResultingTraceSequence();
        List<AppState> stateSequence = rr.getResultingStateSequence();

        if(strictMode && rr.getConflictFlag())
            throw new RuntimeException("Something is wrong");

        tree.update(staringAppState, inputSequence, observationSequence, stateSequence, rr.getConflictFlag());
        graph.update(tree.getCorrespondingTreeState(staringAppState), inputSequence, rr.getConflictFlag());
    }


    public void dumpInternal(String fileprefix){
        try{
            tree.writeTo(new FileWriter(fileprefix + ".lstar.dot"));
            graph.writeTo(new FileWriter(fileprefix + ".graph.dot"));
        }
        catch(Exception e){
            throw new RuntimeException("Something is wrong", e);
        }
    }

    @Override
    public List<String> getOptionString(){
        return null;
    }

    @Override
    public void finish(){}
}

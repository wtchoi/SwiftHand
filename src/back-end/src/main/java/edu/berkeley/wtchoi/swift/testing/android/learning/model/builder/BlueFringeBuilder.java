package edu.berkeley.wtchoi.swift.testing.android.learning.model.builder;

import edu.berkeley.wtchoi.swift.testing.android.learning.model.GraphModel;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.Model;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.State;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.builder.slection.FittestMergeSelection;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.builder.slection.PairEvaluation;
import edu.berkeley.wtchoi.collection.CollectionUtil;
import edu.berkeley.wtchoi.collection.Pair;

import java.io.FileWriter;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/29/13
 * Time: 6:23 PM
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
public class BlueFringeBuilder{
    GraphModel hypothesis;
    private MergeSelection.Factory selectorFactory;
    private boolean plotMergeSteps = false;

    public BlueFringeBuilder(Model tree){
        hypothesis = new GraphModel(tree);
        selectorFactory = new FittestMergeSelection.Factory();
    }

    public void plotMergeSteps(){
        plotMergeSteps = true;
    }

    public enum MergeOption{
        ToFittest;
    }

    private final Set<State> redStates = new TreeSet<State>();
    private final Set<State> blueStates = new TreeSet<State>();


    public void setMergeOption(MergeOption option){
        switch(option){
            case ToFittest:
                selectorFactory = new FittestMergeSelection.Factory();
                break;
        }
    }

    public GraphModel build(){
        State root = hypothesis.getRoot();
        promoteToRed(root);

        while(!blueStates.isEmpty()){
            LinkedList<Pair<Integer, Integer>> simulatedSequence = new LinkedList<Pair<Integer, Integer>>();
            LinkedList<Pair<Integer, Integer>> realSequence = new LinkedList<Pair<Integer, Integer>>();

            //Evaluate pairs
            MergeSelection selection = selectorFactory.create(hypothesis, blueStates, redStates, simulatedSequence);

            //promote unique blue state to red, if exists
            if(selection.uniqueState != null){
                promoteToRed(selection.uniqueState);
                continue;
            }

            //merge selected pair
            Pair<State,State> selectedPair = selection.selectedPair;
            State pivot = selectedPair.fst;
            blueStates.remove(selectedPair.snd);
            try{
                if(plotMergeSteps)
                    hypothesis.forceWriteTo(new FileWriter("/tmp/graph.before.merge.dot"), false);
            }
            catch(Exception ignore){}
            if(pivot.id == 104 && selectedPair.snd.id == 9047){
                PairEvaluation.evaluate(pivot, selectedPair.snd, null);
            }
            if(pivot.id == 2145 && selectedPair.snd.id == 5732){
                PairEvaluation.evaluate(pivot, selectedPair.snd, null);
            }
            try{
                hypothesis.merge(pivot, selectedPair.snd, realSequence);
            }
            catch(NoSuchElementException e){
                String l1 = ("sim  : " + CollectionUtil.stringOf(simulatedSequence, "[", ",", "]\n"));
                String l2 = ("real : " + CollectionUtil.stringOf(realSequence, "[", ",", "]\n"));
                String l3 = "fail to merge " + pivot.id  + " and " + selectedPair.snd.id;
                System.out.println(l1 + l2 + l3);
                throw new RuntimeException(l1 + l2 + l3, e);
            }

            for(State red:redStates){
                InnerLoop:

                for(State child: red.getChildren()){
                    if(child.isYetToVisit) continue InnerLoop;
                    if(!blueStates.contains(child) && !redStates.contains(child)){
                        blueStates.add(child);
                    }
                }
            }

        }
        return hypothesis;
    }

    private void promoteToRed(State s){
        blueStates.remove(s);
        redStates.add(s);

        for(State child :s.getChildren()){
            if(!child.isYetToVisit){
                blueStates.add(child);
            }
        }
    }



    abstract public static class MergeSelection {
        public State uniqueState;
        public Pair<State,State> selectedPair;

        public static abstract class Factory {
            public abstract MergeSelection create(Model model, Set<State> blueSet, Set<State> redSet, LinkedList<Pair<Integer, Integer>> record);
        }
    }
}

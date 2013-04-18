package edu.berkeley.wtchoi.swift.testing.android.learning.model.builder.slection;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.State;
import edu.berkeley.wtchoi.collection.CPair;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/4/13
 * Time: 7:39 PM
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

//Stateless pair evaluator algorithm class
public class PairEvaluation {
    private static TreeMap<State, TreeMap<ICommand, State>> mergeSimulationTable = new TreeMap<State, TreeMap<ICommand, State>>();

    //return -1 if enabledInputs are different
    //return 0  if one of each is yetToVisit
    public static int evaluate(State red, State blue, LinkedList<Pair<Integer, Integer>> record){
        mergeSimulationTable.clear();

        if(red.isYetToVisit || blue.isYetToVisit) return 0;
        if(red.output.compareTo(blue.output) != 0)
            return -1;

        handleParent(red, blue);
        return evaluateDFS(red, blue, record);
    }

    //Assume parents are handled
    private static int evaluateDFS(State red, State blue, LinkedList<Pair<Integer, Integer>> record){
        if(record != null)
            record.add(new Pair<Integer, Integer>(red.id, blue.id));

        LinkedList<Pair<State, State>> thisLevel = new LinkedList<Pair<State, State>>();

        for(ICommand cmd:blue.output){
            State blueChild = blue.getNext(cmd);
            State redChild = lookupMST(red, cmd);

            if(!blueChild.isYetToVisit){
                if(redChild.isYetToVisit){
                    updateMST(red, cmd, blueChild);
                }
                else{
                    if(redChild.output.compareTo(blueChild.output) != 0)
                        return -1;

                    thisLevel.addLast(new Pair<State,State>(redChild, blueChild));
                }
            }
        }

        int sumChildScore = 0;
        for(Pair<State, State> pair : thisLevel){
            int childScore = evaluateDFS(pair.fst, pair.snd, record);
            if(childScore == -1) return -1;
            sumChildScore += childScore;
        }

        return sumChildScore + 1;
    }


    private static void handleParent(State red, State blue){
        for(CPair<State, ICommand> pair : blue.getParents()){
            updateMST(pair.getFirst(), pair.getSecond(), red);
        }
    }

    private static void updateMST(State state, ICommand cmd, State next){
        if(!mergeSimulationTable.containsKey(state))
            mergeSimulationTable.put(state, new TreeMap<ICommand, State>());

        TreeMap<ICommand, State> map = mergeSimulationTable.get(state);
        map.put(cmd, next);
    }

    private static State lookupMST(State state, ICommand cmd){
        Map<ICommand, State> map = mergeSimulationTable.get(state);
        if(map == null) return state.getNext(cmd);

        State s = map.get(cmd);
        if(s == null) return state.getNext(cmd);

        return s;
    }
}

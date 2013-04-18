package edu.berkeley.wtchoi.swift.testing.android.learning.model.builder.slection;

import edu.berkeley.wtchoi.swift.testing.android.learning.model.Model;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.State;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.builder.BlueFringeBuilder;
import edu.berkeley.wtchoi.swift.util.RandomUtil;
import edu.berkeley.wtchoi.collection.CPair;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/4/13
 * Time: 7:04 PM
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
public class FittestMergeSelection extends BlueFringeBuilder.MergeSelection {

    Model model;
    Set<State> blueSet;
    Set<State> redSet;
    LinkedList<Pair<Integer, Integer>> record;

    protected RandomUtil rand = RandomUtil.getSharedInstance();

    private FittestMergeSelection(Model model, Set<State> blueSet, Set<State> redSet, LinkedList<Pair<Integer, Integer>> record){
        this.model = model;
        this.blueSet = blueSet;
        this.redSet = redSet;
        this.record = record;
        selectFittest();
    }

    protected void selectFittest(){
        uniqueState = null;
        selectedPair = null;

        Vector<CPair<State,State>> fittestPairs = new Vector<CPair<State, State>>();
        Vector<State> uniqueCandidates = new Vector<State>();
        int highestScore = 0;

        BlueLoop:
        for(State blue:blueSet){
            int blueScore = -1;
            for(State red:redSet){
                int score = PairEvaluation.evaluate(red, blue, null);

                if(uniqueCandidates.size() > 0 && score != -1) continue BlueLoop;
                if(score > blueScore){
                    blueScore = score;
                    if(score > highestScore){
                        highestScore = score;
                        fittestPairs.clear();
                        fittestPairs.add(new CPair<State,State>(red, blue));
                    }
                    else if(score == highestScore){
                        fittestPairs.add(new CPair<State,State>(red, blue));
                    }
                }
            }

            if(blueScore == -1){
                uniqueCandidates.add(blue);
            }
        }

        if(!uniqueCandidates.isEmpty()){
            uniqueState = rand.pick(uniqueCandidates);
        }
        else if(!fittestPairs.isEmpty()){
            selectedPair = rand.pick(fittestPairs);
            PairEvaluation.evaluate(selectedPair.fst, selectedPair.snd, record);
        }
    }

    public static class Factory extends BlueFringeBuilder.MergeSelection.Factory{
        public BlueFringeBuilder.MergeSelection create(Model model, Set<State> blueSet, Set<State> redSet, LinkedList<Pair<Integer, Integer>> record){
            return new FittestMergeSelection(model, blueSet, redSet, record);
        }
    }
}

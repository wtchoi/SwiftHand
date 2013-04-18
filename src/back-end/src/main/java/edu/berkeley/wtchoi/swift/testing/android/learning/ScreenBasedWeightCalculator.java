package edu.berkeley.wtchoi.swift.testing.android.learning;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.Model;
import edu.berkeley.wtchoi.swift.testing.android.learning.model.State;
import edu.berkeley.wtchoi.swift.util.RandomUtil;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.Pair;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/24/13
 * Time: 2:38 AM
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
class ScreenBasedWeightCalculator implements RandomUtil.WeighCalculator<State>{
    TreeMap<CSet<ICommand>, Integer> screenCount = new TreeMap<CSet<ICommand>, Integer>();
    LearningWithScoutGuide guide;

    public ScreenBasedWeightCalculator(Model model, LearningWithScoutGuide guide){
        this.guide = guide;

        Set<State> states = model.getStates();
        for(State state:states){
            if(!state.isYetToVisit){
                CSet<ICommand> palette = state.output;
                int newVal = 1;
                if(screenCount.containsKey(palette))
                    newVal = screenCount.get(palette) + 1;
                screenCount.put(palette, newVal);
            }
        }
    }

    //Assumption: only frontiers are passed as an argument
    @Override
    public double weight(State state){
        if(!state.isYetToVisit) return 0;
        try{
            Pair<State, ICommand> pair = state.getParents().iterator().next();
            State parent = pair.getFirst();
            return (1.0 / ((double) screenCount.get(parent.output))) + (guide.observedCommand.contains(pair.getSecond())?0.0:2.0);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}

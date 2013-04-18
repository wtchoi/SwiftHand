package edu.berkeley.wtchoi.swift.testing.android.learning;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;
import edu.berkeley.wtchoi.swift.testing.android.AppResult;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.collection.CSet;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/9/13
 * Time: 9:57 PM
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
public class ExecutionFollower {

    protected final CSet<ICommand> tabooSet = new CSet<ICommand>();

    public void reset(){
        tabooSet.clear();
    }

    public CSet<ICommand> getTabooSet(){
        return tabooSet;
    }

    public void add(AppResult result){
        Iterator<ICommand> cmdIter = result.getExecutedCommands().iterator();
        Iterator<AppState> stateIter = result.getResultingStateSequence().iterator();
        Iterator<CompressedLog> logIter = result.getResultingTraceSequence().iterator();

        AppState startingState = result.getStaringState();
        while(cmdIter.hasNext()){
            ICommand cmd = cmdIter.next();
            AppState resultingState = stateIter.next();

            if(resultingState.isStop()){
                tabooSet.clear();
            }
            else{
                CompressedLog log = logIter.next();

                //if(log.size() == 0 && checkShallowEquivalence(startingState, resultingState)){
                if(checkShallowEquivalence(startingState, resultingState)){
                    //TODO
                    tabooSet.add(cmd);
                }
                else{
                    tabooSet.clear();
                }
            }
        }
    }

    protected boolean checkShallowEquivalence(AppState start, AppState result){
        //TODO
        if(result.isStop()) return false;
        return start.getPalette().compareTo(result.getPalette()) == 0;
    }
}


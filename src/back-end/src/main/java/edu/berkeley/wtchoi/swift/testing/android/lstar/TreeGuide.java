package edu.berkeley.wtchoi.swift.testing.android.lstar;

import edu.berkeley.wtchoi.swift.testing.android.ViewToEvents;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;
import edu.berkeley.wtchoi.swift.testing.android.AppRequest;
import edu.berkeley.wtchoi.swift.testing.android.AppResult;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.swift.testing.android.AppTestingGuide;
import edu.berkeley.wtchoi.swift.util.statistics.SimpleTracker;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.CVector;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/24/12
 * Time: 9:30 PM
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


public class TreeGuide implements AppTestingGuide {

    private static final int preSearchBound = 0;
    private TreeLearner learner;

    private CSet<ICommand> defaultPalette;
    private TreeTestingObserver observer;

    public TreeGuide(){
        defaultPalette = new CSet<ICommand>();
    }

    public void setInitialState(AppState s){

        CSet<ICommand> initialPalette = getPalette(s);

        learner = new TreeLearner(initialPalette);
        observer.setLearner(this.learner);
    }

    private AppRequest waitingRequest = null;
    private int preSearchCount = 0;

    private SimpleTracker learningTracker = new SimpleTracker();


    private CSet<ICommand> getPalette(AppState s){
        return ViewToEvents.getRepresentativePoints(s.getViewInfo(), new CSet<ICommand>());
    }

    private CList<ICommand> currentState;
    public AppRequest getRequest(AppState s){
        currentState = new CVector<ICommand>(s.getHistory());
        AppRequest request = getRequestImp();
        request.setRequestTrace();
        return request;
    }

    private AppRequest getRequestImp(){
        learningTracker.start();
        if(waitingRequest != null){
            CList<ICommand> recommendation = learner.recommend(currentState);
            if(preSearchCount < preSearchBound && recommendation != null){
                preSearchCount++;
                return new AppRequest(recommendation);
            }
            else{
                preSearchCount = 0;
                AppRequest request = waitingRequest;
                waitingRequest = null;
                return request;
            }
        }
        else{
            TreeLearner.ExploreRequest<ICommand> request = learner.getRequest(currentState);
            AppRequest appRequest  = new AppRequest(request.input);
            appRequest.setExtra(request);

            if(!request.fromCurrentState){
                appRequest.setRestartRequest();
                waitingRequest = appRequest;
                return getRequestImp();
            }

            return appRequest;
        }
    }

    public void receiveResult(AppRequest request, AppResult result){
        learningTracker.stop();
        CList<ICommand> input = request.getInputSequence();
        Iterator<AppState> stateIterator = result.getResultingStateSequence().iterator();
        Iterator<CompressedLog> logIterartor = result.getResultingTraceSequence().iterator();

        CList<Observation> output = new CVector<Observation>(input.size());

        AppState state;
        CompressedLog log;

        while(stateIterator.hasNext()){
            state = stateIterator.next();
            if(state.isStop()){
                output.add(Observation.getStopObservation());
                break;
            }
            else{
                log = logIterartor.next();
                CSet<ICommand> palette = getPalette(state);
                palette.addAll(defaultPalette);
                TransitionInfo ti = new TransitionInfo(log);
                output.add(new Observation(palette, ti));
            }
        }

        CList<ICommand> startingStateToPass = new CVector<ICommand>(result.getStaringState().getHistory());
        CList<ICommand> inputToPass = new CVector<ICommand>(input);
        TreeLearner.ExploreResult<ICommand,Observation> resultToPass = new TreeLearner.ExploreResult<ICommand, Observation>(startingStateToPass, inputToPass, output);
        resultToPass.query = (TreeLearner.ExploreRequest<ICommand>) request.getExtra();
        learner.learn(resultToPass);
    }

    public void setObserver(TreeTestingObserver obs){
        observer = obs;
    }

    @Override
    public List<String> getOptionString(){
        List<String> list = new Vector<String>();
        list.add("mean time to learning : " + learningTracker.average());
        return list;
    }


    @Override
    public void finish(){}
}

package edu.berkeley.wtchoi.swift.testing;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/25/12
 * Time: 5:16 PM
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
public class Testing<Request, Result, State> {
    private long timeout;
    private long startTime;

    private LinkedList<TestingObserver<Request, Result, State>> observers = new LinkedList<TestingObserver<Request, Result, State>>();
    public Guide<Request, Result, State> guide;
    public TargetProgram<Request, Result, State> target;

    public void setTimeout(long t){
        timeout = t;
    }

    public void addObserver(TestingObserver<Request, Result, State> o){
        observers.addLast(o);
    }

    public void removeObserver(TestingObserver<Request, Result, State> o){
        observers.remove(o);
    }

    public void setGuide(Guide<Request,Result,State> g){
        guide = g;
    }

    public void setTarget(TargetProgram<Request, Result, State> t){
        target = t;
    }

    public void run(){
        startTime = System.currentTimeMillis();
        invokeObserveOnBegin();

        try{
            invokeObserveOnPrepareBegin();
            {
                target.init();
                State s = target.currentState();
                guide.setInitialState(s);
            }
            invokeObserveOnPrepareEnd();


            invokeObserveOnMainTestBegin();
            while(!checkTimeout()){
                doSomething();
            }
            target.close();
            invokeObserverOnMainTestEnd();
            invokeObserveOnEnd();
        }
        catch (Exception e)
        {
            target.close();
            invokeObserveOnException(e);
        }
        guide.finish();
        invokeObserveFinish();
    }

    protected boolean checkTimeout(){
        long currentTime = System.currentTimeMillis();
        return currentTime- startTime > timeout;
    }


    protected void doSomething(){
        invokeObserverOnTestItemBegin();

        State state = target.currentState();

        invokeObserverOnDecisionBegin();
        Request request = guide.getRequest(state);
        invokeObserverOnnDecisionEnd();

        invokeObserverOnExecutionBegin();
        Result result = target.execute(request);
        invokeObserveOnExecutionEnd();

        invokeObserveOnReportBegin();
        guide.receiveResult(request, result);
        invokeObserveOnReportEnd();

        invokeObserveOnTestItemEnd(request, result);
    }

    protected void invokeObserveOnBegin(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onBegin();
        }
    }

    protected void invokeObserveOnPrepareBegin(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onPrepareBegin();
        }
    }

    protected void invokeObserveOnPrepareEnd(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onPrepareEnd();
        }
    }


    protected void invokeObserveOnMainTestBegin(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onMainTestBegin();
        }
    }

    protected void invokeObserverOnMainTestEnd(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onMainTestBegin();
        }
    }

    protected void invokeObserverOnTestItemBegin(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onTestItemBegin();
        }
    }

    protected void invokeObserverOnDecisionBegin(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onDecisionBegin();
        }
    }


    protected void invokeObserverOnnDecisionEnd(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onDecisionEnd();
        }
    }

    protected void invokeObserverOnExecutionBegin(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onExecutionBegin();
        }
    }

    protected void invokeObserveOnExecutionEnd(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onExecutionEnd();
        }
    }

    protected void invokeObserveOnReportBegin(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onReportBegin();
        }
    }

    protected void invokeObserveOnReportEnd(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onReportEnd();
        }
    }

    protected void invokeObserveOnTestItemEnd(Request request, Result result){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onTestItemEnd(request, result);
        }
    }

    protected void invokeObserveOnEnd(){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onEnd();
        }
    }

    protected void invokeObserveOnException(Throwable e){
        for(TestingObserver<Request, Result, State> observer:observers){
            observer.onException(e);
        }
    }

    protected void invokeObserveFinish(){

    }
}



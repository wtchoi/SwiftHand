package edu.berkeley.wtchoi.swift.driver.drone;

import android.app.Activity;
import android.util.Log;
import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;
import edu.berkeley.wtchoi.logger.Logger;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/23/12
 * Time: 11:16 AM
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
public class ThreadTracker {
    final LinkedList<SLog> myStack = new LinkedList<SLog>();
    final CompressedLog myTrace = new CompressedLog();
    final ProgramPointSet myDecisionPoints = new ProgramPointSet();

    SLog myLastBB;
    boolean panicMode = false;
    boolean instrumentMode = false;
    boolean isActive = false;
    boolean flagTraceLogging = false;

    long tid;
    Thread thread;
    SupervisorImp s = null;

    public ThreadTracker(SupervisorImp _s){
        s = _s;
        thread = Thread.currentThread();
        tid = thread.getId();
    }

    private void enterInstrumentMode(){
        if(!panicMode) return;
        if(instrumentMode){
            try{throw new RuntimeException();}
            catch(Throwable e){
                s.postPanic("Entering Instrumentation inside Instrumentation!", e);
            }
            return;
        }

        instrumentMode = true;
    }

    private void exitInstrumentMode(){
        instrumentMode = false;
    }

    private void tryActivePost(){
        if(!isActive){
            isActive = true;
            s.entryCounter.incrementAndGet();
            s.deepTransitionFlag.set(true);
            flagTraceLogging = s.flagTraceLogging;
        }
        else{
            if(myStack.size() == 0){
                isActive = false;
                s.entryCounter.decrementAndGet();
            }
        }
    }

    private void pushToList(SLog slog){
        //Log.d("wtchoi","tid = " + tid + ", " + slog.toString());
        CompressedLog lst;
        lst = myTrace;
        lst.add(slog);

        if(lst.size() % 2000 == 0)
            Log.d("wtchoi", "list length(" + tid + ") = " + lst.size());
    }

    private void pushToStack(SLog slog){
        LinkedList<SLog> stk;
        stk = myStack;
        stk.add(slog);
    }

    private void popFromStack(){
        LinkedList<SLog> stack;
        stack = myStack;
        stack.removeLast();
    }

    private SLog getStackTop(){
        LinkedList<SLog> stack;
        stack = myStack;
        if(stack.isEmpty()) return null;
        return stack.getLast();
    }

    private void removeLastLog(){
        CompressedLog lst;
        lst = myTrace;
        lst.removeLast();
    }

    public void logEnter(short fid) throws Exception{
        //Logger.log("ENTERING(tid=" + tid +  ", fid=" + fid + ")");
        SLog log = SLog.getEnter(fid);
        int flag = 1; //set error flag. must be reset to zero


        enterInstrumentMode();
        if(!panicMode){
            SLog top = getStackTop();

            if(top == null || top.type == SLog.CALL){
                pushToStack(log);
                tryActivePost();
                //entryCounter.incrementAndGet();

                if(flagTraceLogging){
                    if(top != null) top.aux++;

                    myLastBB = null;
                    pushToList(log);
                }
                flag = 0;
            }

            assertZero(flag, fid, "logEnter", null);
            exitInstrumentMode();
        }
    }

    public void logEnterCLINIT(short  fid){
        //no check for CLINIT
        enterInstrumentMode();
        if(!panicMode){
            SLog log = SLog.getEnterCLINIT(fid);

            pushToStack(log);
            tryActivePost();

            if(flagTraceLogging){
                SLog top = getStackTop();
                if(top != null && top.type == SLog.CALL) top.aux++;

                tryToPushLastPP(fid);
                pushToList(log);
                myLastBB = null;
            }

            exitInstrumentMode();
        }

    }

    public void logExit(short fid) throws Exception{
        //synchronized (sLists){
        enterInstrumentMode();
        if(!panicMode){
            SLog log = SLog.getExit(fid);
            int flag = 0;

            flag = unrollStack(fid);
            if(flag == 0){
                if(flagTraceLogging) {
                    pushToList(log);
                }
            }

            assertZero(flag, fid, "logExit", null);
            exitInstrumentMode();
            tryActivePost();
        }
    }

    public void logUnroll(short fid, Exception e) throws Exception{
        SLog log, top;

        //handle exception raised by instrumented code
        if(instrumentMode){
            s.postPanic("Instrumentation Failure", e);
            return;
        }

        enterInstrumentMode();
        if(!panicMode){
            int flag = 2;  //Set error flag to 2. unrollStack call must reset it to 0
            top = getStackTop();

            //CASE 1: exception was raised inside invoked method
            //Resulting log will have form "..., ENTER(ENTER_CLINIT), ..., PP, CALL, {inside invoked method}, UNROLL_CATCH, UNROLL_THROW"
            if(top.type == SLog.CALL && top.fid == fid){
                popFromStack();
                flag = unrollStack(fid);
                if(flag == 0){
                    if(flagTraceLogging){
                        log = SLog.getUnrollCatch(fid);
                        pushToList(log);
                        log = SLog.getThrowUnroll(fid);
                        pushToList(log);
                    }
                }
            }
            //CASE 2: exception was raised by instruction
            //Resulting log will have form "..., ENTER(CLINIT), ..., PP, UNROLL_CATCH, UNROLL_THROW"
            else if((top.type == SLog.ENTER_CLINIT || top.type == SLog.ENTER) && top.fid == fid){
                flag = unrollStack(fid);
                if(flagTraceLogging && flag == 0){
                    flag = tryToPushLastPP(fid);
                    if(flag == 0){
                        log = SLog.getUnrollCatch(fid);
                        pushToList(log);
                        log = SLog.getThrowUnroll(fid);
                        pushToList(log);
                    }
                }
            }
            assertZero(flag, fid, "logUnroll", e);
            exitInstrumentMode();
            tryActivePost();
        }
    }

    private int tryToPushLastPP(int fid){
        SLog bb = myLastBB;
        if(bb == null) return 4;

        if(bb.type == SLog.PP_EXTRA){
            pushToList(bb);
        }
        return 0;
    }

    public void logThrow(short fid) throws Exception{
        SLog log = SLog.getThrow(fid);
        int flag;

        enterInstrumentMode();
        if(!panicMode){
            flag = unrollStack(fid);
            if(flag == 0){
                if(flagTraceLogging){
                    pushToList(log);
                }
            }
            assertZero(flag,fid, "logThrow", null);
            exitInstrumentMode();
            tryActivePost();
        }
    }

    public void logCatch(short fid) throws Exception{
        SLog top;
        SLog log;
        int flag = 2; //set error flag. have to be reset to zero.

        enterInstrumentMode();
        if(!panicMode){
            top = getStackTop();

            //CASE 1: exception was raised inside invoked method
            //Resulting log will have form "... ENTER(CLINIT), ..., PP, CALL, {inside invoked method}, CATCH"
            if(top.type == SLog.CALL && top.fid == fid){
                popFromStack();
                if(flagTraceLogging){
                    log = SLog.getCatch(fid);
                    pushToList(log);
                }
                //snooze();
                flag = 0;
            }
            //CASE 2: exception was raised by instruction
            //Resulting log will have form "..., ENTER(CLINIT), ... , PP, CATCH"
            else if(flagTraceLogging && (top.type == SLog.ENTER || top.type == SLog.ENTER_CLINIT) && top.fid == fid){
                flag = tryToPushLastPP(fid);
                log = SLog.getCatch(fid);
                pushToList(log);
            }
        }

        assertZero(flag, fid, "logCatch", null);
        exitInstrumentMode();
    }

    private int unrollStack(int fid){
        SLog stackTop = getStackTop();
        if((stackTop.type == SLog.ENTER || stackTop.type == SLog.ENTER_CLINIT) && stackTop.fid == fid){
            popFromStack();
            return 0;
        }
        return 1;
    }

    public void logCall(short fid){
        enterInstrumentMode();
        if(!panicMode){
            SLog log = SLog.getCall(fid);
            pushToStack(log);
            if(flagTraceLogging){
                pushToList(log);
            }

            exitInstrumentMode();
        }
    }

    public void logReturn(short fid) throws Exception{
        enterInstrumentMode();
        if(!panicMode){
            int flag = 1; //set error flag. must be reset to 0.

            SLog stackTop = getStackTop();
            if(stackTop.type == SLog.CALL && stackTop.fid == fid){
                popFromStack();
                //Trace Logging
                if(flagTraceLogging){
                    //CASE1: all invoked methods are out-side method. we don't have trace information to wrap
                    if(stackTop.aux == 0){
                        removeLastLog();
                    }
                    //CASE2: at least, one of invoked methods are not out-side method
                    else{
                        SLog log = SLog.getReturn(fid);
                        pushToList(log);
                    }
                }

                flag = 0;
            }

            assertZero(flag, fid, "logReturn", null);
            exitInstrumentMode();
        }
    }

    public void logReceiver(Object obj, short fid){
        if(!panicMode){
            if(flagTraceLogging){
                enterInstrumentMode();
                SLog log = SLog.getReceiver(System.identityHashCode(obj), fid);
                pushToList(log);
            }
            exitInstrumentMode();
        }
    }

    public void logDecisionPoint(int pp, short fid){
        enterInstrumentMode();
        if(!panicMode){
            SLog log = SLog.getPP(pp, fid);
            myLastBB = log;
            pushToList(log);
            myDecisionPoints.add(pp,fid);
            exitInstrumentMode();
        }
    }

    public void logProgramPoint(int pp, short fid){
        enterInstrumentMode();
        if(!panicMode){
            SLog log = SLog.getPP(pp, fid);
            myLastBB = log;
            pushToList(log);
            exitInstrumentMode();
        }
    }


    public void logProgramPointExtra(int pp, short fid){
        enterInstrumentMode();
        if(!panicMode){
            if(flagTraceLogging){
                SLog log = SLog.getPPExtra(pp, fid);
                myLastBB = log;
            }
            exitInstrumentMode();
        }
    }

    private HashMap<Activity, Integer> activityMethodCallCount = new HashMap<Activity, Integer>();
    private void increaseCount(Activity a){
        if(!activityMethodCallCount.containsKey(a)){
            activityMethodCallCount.put(a,1);
        }
        else{
            activityMethodCallCount.put(a,activityMethodCallCount.get(a)+1);
        }
    }

    private boolean decreaseAndCheckZero(Activity a){
        if(activityMethodCallCount.get(a) == 1){
            activityMethodCallCount.remove(a);
            return true;
        }
        else{
            activityMethodCallCount.put(a,activityMethodCallCount.get(a) - 1);
            return false;
        }
    }

    public void logActivityCreatedEnter(Activity a){
        if(!panicMode){
            increaseCount(a);
        }
    }

    public void logActivityCreatedExit(Activity a){
        if(!panicMode){
            if(decreaseAndCheckZero(a)){
                synchronized (s.sLists){
                    s.activityCount.incrementAndGet();
                    s.activityStates.put(a, new ActivityState());
                }
            }
        }
    }

    public void logResumeEnter(Activity a){
        if(!panicMode){
            increaseCount(a);
        }
    }

    public void logResumeExit(Activity a){
        if(!panicMode){
            if(decreaseAndCheckZero(a)){
                synchronized (s.sLists){
                    ActivityState state = s.activityStates.get(a);
                    s.activeActivityCount.incrementAndGet();

                    if(state.isPaused()){
                        s.pausedActivityCount.decrementAndGet();
                    }
                    else if(state.isStart()){
                        s.startActivityCount.decrementAndGet();
                    }
                    state.setActive();
                    s.printActivityStates();
                }
            }
        }
    }

    public void logStartEnter(Activity a){
        if(!panicMode){
            increaseCount(a);
        }
    }

    public void logStartExit(Activity a){
        if(!panicMode){
            if(decreaseAndCheckZero(a)){
                synchronized (s.sLists){
                    if(!s.defaultActivityRegisterd) s.registerDefaultActivity(a);
                    ActivityState state = s.activityStates.get(a);

                    s.startActivityCount.incrementAndGet();
                    if(state.isStop()){
                        s.stopActivityCount.decrementAndGet();
                    }
                    state.setStart();
                    s.printActivityStates();
                }
            }
        }
    }

    public void logPauseEnter(Activity a){
        if(!panicMode){
            increaseCount(a);
        }
    }

    public void logPauseExit(Activity a){
        if(!panicMode){
            if(decreaseAndCheckZero(a)){
                synchronized (s.sLists){
                    ActivityState t = s.activityStates.get(a);
                    s.activeActivityCount.decrementAndGet();
                    s.pausedActivityCount.incrementAndGet();
                    t.setPause();
                    s.printActivityStates();
                }
            }
        }
    }


    public void logStopEnter(Activity a){
        if(!panicMode){
            increaseCount(a);
        }
    }

    public void logStopExit(Activity a){
        if(!panicMode){
            if(decreaseAndCheckZero(a)){
                synchronized (s.sLists){
                    s.pausedActivityCount.decrementAndGet();
                    s.stopActivityCount.incrementAndGet();
                    ActivityState t = s.activityStates.get(a);
                    t.setStop();
                    s.printActivityStates();
                }
            }
        }
    }

    public void logDestroyEnter(Activity a){
        if(!panicMode){
            increaseCount(a);
        }
    }

    public void logDestroyExit(Activity a){
        if(!panicMode){
            if(decreaseAndCheckZero(a)){
                synchronized (s.sLists){
                    ActivityState state = s.activityStates.get(a);
                    if(state.isActive()) s.activeActivityCount.decrementAndGet();
                    else if(state.isPaused()) s.pausedActivityCount.decrementAndGet();
                    else if(state.isStart()) s.startActivityCount.decrementAndGet();
                    else if(state.isStop()) s.stopActivityCount.decrementAndGet();
                    s.activityCount.decrementAndGet();
                    s.activityStates.remove(a);
                }
            }
        }
    }

    private void assertZero(int flag, int fid, String context, Exception e) throws Exception{
        if(flag != 0){
            if(e == null){
                try{
                    e = new RuntimeException();
                    throw e;
                }
                catch(Exception ee){
                    e = ee;
                }
            }
            panicMode = true;
            s.postPanic(context + ": Something is wrong! (flag=" + flag + ", fid=" + fid + ")", e);
        }
    }

    public void printAllTrace(){
        Logger.log("=== PRINT CORE TRACE(tid = " + tid + ") ===");

        int i = 1;
        synchronized (s.sLists){
            for(SLog log:myTrace){
                if(log.type != SLog.PP && log.type != SLog.PP_EXTRA){
                    Logger.log("#" + i + ":" + log.toString());
                }
                i++;
            }
        }
    }

    public void printLastTrace(int threshold){
        int start = 1;
        Logger.log("=== PRINT TRACE(tid = " + tid + ") ===");

        if(myTrace.size() > threshold){
            Logger.log("trace is too long. last 1000 items are:");
            start = myTrace.size() - threshold;
        }

        int i = 1;
        synchronized (s.sLists){
            for(SLog log:myTrace){
                if(i  >= start){
                    Logger.log("#" + i + ":" + log.toString());
                }
            i++;
            }
        }
    }

    public void printStack(){
        Logger.log("=== PRINT STACK(tid = " + tid + ") ===");
        synchronized (s.sLists){
            for(SLog log:myStack){
                Logger.log(log.toString());
            }
        }
    }
}

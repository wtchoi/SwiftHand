package edu.berkeley.wtchoi.swift.driver.drone;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 6:08 PM
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

import android.app.Activity;
import android.app.Application;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import edu.berkeley.wtchoi.swift.driver.DriverPacket;
import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;
import edu.berkeley.wtchoi.logger.Logger;
import edu.berkeley.wtchoi.util.TcpChannel;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class SupervisorImp extends Thread{
    final HashMap<Long, CompressedLog> sLists = new HashMap<Long, CompressedLog>();
    final HashMap<Long, LinkedList<SLog>> sStacks = new HashMap<Long, LinkedList<SLog>>();
    final HashMap<Long, ProgramPointSet> sDecisionPoints = new HashMap<Long, ProgramPointSet>();

    final Set<ThreadTracker> trackers = new HashSet<ThreadTracker>();
    final HashMap<Long, ThreadTracker> trackerMap = new HashMap<Long, ThreadTracker>();

    //final Set<Long> threads = new HashSet<Long>();

    long mainTid;

    private volatile long errorPostThread;
    private volatile String errorPostMsg;
    private volatile Throwable errorPostException;
    private volatile boolean instrumentFailure = false;
    private volatile boolean panicFlag = false;

    final ThreadLocal<ThreadTracker> myTL = new ThreadLocal<ThreadTracker>();

    boolean flagTraceLogging = false;
    final AtomicInteger entryCounter = new AtomicInteger(0);

    private int tickcount = 0;

    final HashMap<Activity,ActivityState> activityStates = new HashMap<Activity, ActivityState>();
    final AtomicInteger activityCount = new AtomicInteger(0);
    final AtomicInteger activeActivityCount = new AtomicInteger(0);
    final AtomicInteger stopActivityCount = new AtomicInteger(0);
    final AtomicInteger pausedActivityCount = new AtomicInteger(0);
    final AtomicInteger startActivityCount = new AtomicInteger(0);

    final AtomicBoolean deepTransitionFlag = new AtomicBoolean(false);

    private volatile AbstractState state;
    private ApplicationWrapper app_wrapper;

    volatile TcpChannel<DriverPacket> channel;

    int TICKCOUNT = 5;
    int TICKINTERVAL = 100;
    int TICKSNOOZE = 3;
    int STABLECOUNT = 1;
    int CHANNEL_TIMEOUT = 30000;
    int TRANSITON_TIMEOUT = 200;

    //Application properties
    private int screen_x;
    private int screen_y;
    private WindowManager windowManager;
    private Display defaultDisplay;

    //internal flag variables
    volatile boolean restartIntended = false;
    volatile boolean defaultActivityRegisterd = false;

    final ViewHelper viweHelper = new ViewHelper(this);

    private void checkInit(){
        boolean flag = myTL.get() != null;
        if(flag) return;

        Thread t = Thread.currentThread();
        installThreadExceptionHandler(t);
        long tid = t.getId();
        ThreadTracker threadTracker = new ThreadTracker(this);

        Logger.log("INITIALIZE THREAD :" + tid);

        synchronized (sLists){
            sLists.put(tid, threadTracker.myTrace);
            sStacks.put(tid, threadTracker.myStack);
            sDecisionPoints.put(tid, threadTracker.myDecisionPoints);

            trackers.add(threadTracker);
            trackerMap.put(tid, threadTracker);
            //threads.add(tid);
        }
        myTL.set(threadTracker);
    }


    public SupervisorImp(){}

    public void prepare(){
        //initialize supervisor data structures
        mainTid = Thread.currentThread().getId();
        installBootExceptionHandler();
        initiateChannel();
    }

    public void registerApplication(Application app){
        app_wrapper = new ApplicationWrapper(app);
    }

    void registerDefaultActivity(Activity defaultActivity){
        defaultActivityRegisterd = true;
        windowManager = defaultActivity.getWindowManager();
        defaultDisplay = windowManager.getDefaultDisplay();

        //defaultDisplay.getSize(size);
        screen_x = defaultDisplay.getWidth();
        screen_y = defaultDisplay.getHeight();
    }


    public void clearData(){
        app_wrapper.clearData();
    }

    public void hideKeyboard(IBinder wt){
        app_wrapper.hideKeyboard(wt);
    }

    public boolean isInputMethodActive(){
        return  app_wrapper.inputMethodActive();
    }

    public boolean isInputMEthodFullScreen(){
        return app_wrapper.inputMethodFullScreen();
    }

    private volatile long supervisorTid;
    private volatile Thread supervisorThread;

    @Override
    public void run(){
        supervisorThread = Thread.currentThread();
        supervisorTid = supervisorThread.getId();
        installDefaultExceptionHandler();
        installShutdownHook();

        tickcount = TICKCOUNT;

        try{
            while(true){
                //Tick Sleep
                //Log.d("wtchoi", "try sleepTick in run : " + tickcount);
                sleepTick();
                if(panicFlag) tryAbort();

                if(tickcount == 0){
                    tickcount = TICKCOUNT;
                    //Log.d("wtchoi", "try run main");
                    this.state.work();
                    this.state = state.next();
                }
                else{
                    tickcount--;
                }
            }
        }
        catch(Throwable e){
            postPanic("Supervisor Crash", e);
            tryAbort();
        }
    }

    private void sleepTick(){
        try{
            Thread.sleep(TICKINTERVAL);
        }
        catch(InterruptedException e){}
    }

    private void initiateChannel(){
        channel = TcpChannel.getServerSide(13338);

        Thread t = new Thread(){
            @Override
            public void run(){
                channel.connect();
            }
        };

        t.start();
        try{
            t.join();
            t.join();
        }
        catch(Exception e){
            //TODO
        }

        Log.d("wtchoi", "stream initialized");
        state = new FirstUnstableState(this);

        //TODO:generate initial report content
        DriverPacket sPacket = DriverPacket.getInitReport(null);
        channel.sendPacket(sPacket);
    }

    void closeApplication(){
        alreadyHandled = true;
        try{
            channel.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        Runtime.getRuntime().halt(0);
        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void start(){
        if(isAlive()) return;
        super.start();
    }


    public Activity getCurrentActivity(){
        synchronized (sLists){
            for(Map.Entry<Activity, ActivityState> e:activityStates.entrySet()){
                if(e.getValue().isActive())
                    return e.getKey();
            }
            return null;
        }
    }

    public int getScreenX(){
        return screen_x;
    }

    public int getScreenY(){
        return screen_y;
    }

    public void logEnter(short fid) throws Exception{
        checkInit();
        myTL.get().logEnter(fid);
    }

    public void logEnterCLINIT(short fid) throws Exception{
        checkInit();
        myTL.get().logEnterCLINIT(fid);
    }

    public void logExit(short fid) throws Exception{
        myTL.get().logExit(fid);
    }

    //called at instrumented handler to handler exception that is not caught by original program
    public void logUnroll(short fid, Exception e) throws Exception{
        myTL.get().logUnroll(fid, e);
    }

    public void logThrow(short fid) throws Exception{
        myTL.get().logThrow(fid);
    }

    //called at user defined handler
    public void logCatch(short fid) throws Exception{
       myTL.get().logCatch(fid);
    }

    public void logCall(short fid){
        myTL.get().logCall(fid);
    }

    public void logReturn(short fid) throws Exception{
        myTL.get().logReturn(fid);
    }

    public void logReceiver(Object obj, short fid){
        myTL.get().logReceiver(obj, fid);
    }

    public void logDecisionPoint(int pp, short fid){
        myTL.get().logDecisionPoint(pp,fid);
    }

    public void logProgramPoint(int pp, short fid){
        myTL.get().logProgramPoint(pp, fid);
    }


    public void logProgramPointExtra(int pp, short fid){
        myTL.get().logProgramPointExtra(pp, fid);
    }

    public void logActivityCreatedEnter(Activity a){
        myTL.get().logActivityCreatedEnter(a);
    }

    public void logActivityCreatedExit(Activity a){
        myTL.get().logActivityCreatedExit(a);
    }

    public void logResumeEnter(Activity a){
        myTL.get().logResumeEnter(a);
    }

    public void logResumeExit(Activity a){
        myTL.get().logResumeExit(a);
    }

    public void logStartEnter(Activity a){
        myTL.get().logStartEnter(a);
    }

    public void logStartExit(Activity a){
        myTL.get().logStartExit(a);
    }

    public void logPauseEnter(Activity a){
        myTL.get().logPauseEnter(a);
    }

    public void logPauseExit(Activity a){
        myTL.get().logPauseExit(a);
    }


    public void logStopEnter(Activity a){
        myTL.get().logStopEnter(a);
    }

    public void logStopExit(Activity a){
        myTL.get().logStopExit(a);
    }

    public void logDestroyEnter(Activity a){
        myTL.get().logDestroyEnter(a);
    }

    public void logDestroyExit(Activity a){
        myTL.get().logDestroyExit(a);
    }


    final private LinkedList<ThreadTracker> garbageTrackers = new LinkedList<ThreadTracker>();
    void collectGarbageTracker(){
        garbageTrackers.clear();
        for(ThreadTracker tracker: trackers){
            if(!tracker.thread.isAlive()) garbageTrackers.add(tracker);
        }

        for(ThreadTracker tracker: garbageTrackers){
            removeTracker(tracker);
        }
    }

    void removeTracker(ThreadTracker tracker){
        long tid = tracker.tid;
        sLists.remove(tid);
        sStacks.remove(tid);
        sDecisionPoints.remove(tid);

        trackers.remove(tracker);
        trackerMap.remove(tid);
    }

    void clearTrace(long tid){
        synchronized (sLists){
            sLists.get(tid).clear();
            sDecisionPoints.get(tid).clear();
        }
    }

    CompressedLog getTrace(long tid){
        synchronized (sLists){
            return sLists.get(tid);
        }
    }



    //ERROR HANDLING
    private Throwable e;

    private void postAbort(String msg, long tid, Throwable e){
        synchronized(sLists){
            errorPostThread = tid;
            errorPostMsg = msg;
            errorPostException = e;
            instrumentFailure = false;
        }
    }

    void postPanic(String msg, Throwable e){
        postPanic(msg, Thread.currentThread().getId(), e);
    }

    void postPanic(String msg, long tid, Throwable e){
        synchronized (sLists){
            errorPostThread = tid;
            errorPostMsg = msg;
            errorPostException = e;
            instrumentFailure = true;
        }
    }


    void tryAbort(){

        Logger.log("Aborting Application (thread " + errorPostThread + ")");
        if(errorPostMsg != null) Logger.log(errorPostMsg);

        if(errorPostException != null){
            Logger.log(errorPostException.getClass().toString());
        }

        if(!(errorPostException instanceof IOException)){
            try{
                channel.sendPacket(DriverPacket.getAckCrash(!instrumentFailure));
                //channel.receivePacket();
                Thread.sleep(100);
            }
            catch(Exception ee){
                ee.printStackTrace();
            }
        }

        if(errorPostException != null){
            errorPostException.printStackTrace();
        }

        try{
            if((!(errorPostException instanceof VerifyError)) && (errorPostThread != supervisorTid)){

                ThreadTracker tracker = trackerMap.get((Long) errorPostThread);
                tracker.printAllTrace();
                tracker.printStack();
//                if(this.instrumentFailure){
                    printActivityStates();
//                }
            }
        }
        catch (Exception e){}
        //closeApplication();
    }

    private volatile UncaughtExceptionHandler handler;

    private void installBootExceptionHandler(){
        handler = new ExceptionHandler(null);
        synchronized (sLists){
            Thread.setDefaultUncaughtExceptionHandler(handler);
        }
    }

    private void installDefaultExceptionHandler(){
        UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        if(this.handler == handler) return;
        synchronized (sLists){
            this.handler = handler;
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(handler));
        }
    }

    private void installThreadExceptionHandler(Thread t){
        UncaughtExceptionHandler handler = t.getUncaughtExceptionHandler();
        t.setUncaughtExceptionHandler(new ThreadExceptionHandler(handler));
    }


    class ThreadExceptionHandler implements UncaughtExceptionHandler{
        Thread t = Thread.currentThread();
        private UncaughtExceptionHandler handler;
        public ThreadExceptionHandler(UncaughtExceptionHandler handler){
            this.handler = handler;
        }

        public void uncaughtException(Thread t, Throwable e){
            synchronized (sLists){
                Logger.log("UNCAUGHT EXCEPTION : " + e.getClass().getCanonicalName());
                Logger.log("tid = " + t.getId() + ", supervisorTid = " + supervisorTid );
                e.printStackTrace();
                if(e instanceof VerifyError){
                    Logger.log("VERIFICATION ERROR");
                    postPanic("Verification Error", t.getId(), e);
                    tryAbort();
                }
                else if(handler != null){
                    Logger.log("PASS to existing handler");
                    handler.uncaughtException(t, e);
                }
                else{
                    Logger.log("PASS to parent handler");
                    ThreadGroup threadGroup = t.getThreadGroup();
                    if(threadGroup == null){
                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(t,e);
                    }
                    else{
                        threadGroup.uncaughtException(t,e);
                    }
                }
            }
        }
    }

    class ExceptionHandler implements Thread.UncaughtExceptionHandler{
        private UncaughtExceptionHandler handler;
        public ExceptionHandler(UncaughtExceptionHandler handler){
            this.handler = handler;
        }

        public void uncaughtException(Thread t, Throwable e){
            synchronized (sLists){
                Logger.log("UNCAUGHT EXCEPTION(global handler)");
                if(supervisorThread == null){
                    Logger.log("Died without supervisor");
                    e.printStackTrace();
                    //closeApplication();
                }
                else{
                    long tid = t.getId();
                    if(e instanceof VerifyError){
                        postPanic("Verification Error", tid, e);
                        tryAbort();
                    }
                    else{
                        if(tid == supervisorTid){
                            postPanic("Supervisor Crash", tid, e);
                            e.printStackTrace();
                            tryAbort();
                        }
                        else if(handler == null){
                            postAbort("Uncaught Exception!", tid, e);
                            tryAbort();
                        }
                        else{
                            try{
                                handler.uncaughtException(t, e);
                            }
                            catch(Throwable ee){
                                //TODO
                            }
                            postAbort("Uncaught Exception", tid, e);
                            tryAbort();
                        }
                    }
                }
            }
        }
    }

    private volatile boolean alreadyHandled = false;
    private void finishingSequence(boolean usermode){
        alreadyHandled = true;
        try{
            channel.sendPacket(DriverPacket.getAckCrash(true));
            //channel.receivePacket();
            //Thread.sleep(100);
        }
        catch(Exception eee){}
        //closeApplication();
    }


    private void installShutdownHook(){
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(){
            @Override
            public void run(){
                Logger.log("SHUTDOWN");
                if(!alreadyHandled){
                    finishingSequence(true);
                }
            }
        });
    }

    @Override
    public void finalize(){
        Logger.log("FINALIZING SUPERVISOR");
        try{
            throw new RuntimeException();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        try{
            channel.close();
        }
        catch(Exception e){

        }
    }

    void printActivityStates(){
        Logger.log("==== Activity States(" + activityCount +") =====");
        for(Activity a:activityStates.keySet()){
            Logger.log(a.toString() + " : " + activityStates.get(a).toString());
        }
    }
}


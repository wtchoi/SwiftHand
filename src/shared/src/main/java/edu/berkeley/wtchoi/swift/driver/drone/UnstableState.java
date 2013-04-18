package edu.berkeley.wtchoi.swift.driver.drone;

import android.util.Log;
import android.view.View;
import edu.berkeley.wtchoi.swift.driver.DriverPacket;
import edu.berkeley.wtchoi.logger.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 5:55 PM
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
public class UnstableState extends AbstractState {
    private AbstractState next = this;

    private int mainPrevsize = 0;
    private Map<Long, Integer> prevsizes  = new HashMap<Long, Integer>();

    private boolean communicationFlag = true;

    private boolean beforeHide = true;
    private int stablecount = 0;
    private long startTime;


    private static int LogThreshHold = 5000;

    int stableCountGoal = s.STABLECOUNT;
    int initialSleep = 0;

    public UnstableState(SupervisorImp s, boolean cf){
        super(s);
        this.communicationFlag = cf;
        startTime  = System.currentTimeMillis();
    }

    public UnstableState(SupervisorImp s){
        super(s);
        startTime  = System.currentTimeMillis();
    }


    private static byte[] viewHash = null;

    private boolean isMayBeStable(int currentILC){
        if (0 == currentILC && s.activeActivityCount.get() != 0){
            byte[] newViewHash = s.viweHelper.getRecentDecorViewMD5();
            if(newViewHash == null){
                viewHash = null;
                return false;
            }

            boolean result = viewHash != null && checkEquality(viewHash, newViewHash);
            viewHash = newViewHash;
            return result;
        }
        return false;
    }

    private boolean checkEquality(byte[] bytes1, byte[] bytes2){
        if(bytes1.length != bytes2.length) return false;
        for(int i =0; i< bytes1.length; i++){
            if(bytes1[i] == bytes2[i]) continue;
            return false;
        }
        return true;
    }


    int WATCHDOGINTERVAL = 10; //tick
    int watchdogcount = 0;
    private boolean onlyMain = false; //to detect the case where intent invoked other activity

    //Inspect whether event handler execution is finished or not
    public void work(){
        if(initialSleep != 0){
            try{
                Thread.sleep(initialSleep);
            }
            catch(Exception ignore){
                Logger.log("cannot sleep initial sleep");
            }
        }


        int currentILC = s.entryCounter.get();
        Log.d("wtchoi", "Unstable State : " + currentILC);

        checkAndHandleUnactivated();

        //We assume that all function accessing s.sStack acquire lock of s.sList.
        if(isMayBeStable(currentILC)){
            if(s.deepTransitionFlag.get() == true){
                stableCountGoal = s.STABLECOUNT;
            }

            if(stablecount == stableCountGoal){
                if(beforeHide){
                    View decorView = s.viweHelper.getRecentDecorView();
                    s.hideKeyboard(decorView.getWindowToken());
                    beforeHide = false;
                    stablecount = stablecount / 2;
                    Logger.log("Unstable State : Hide");
                }
                else{
                    DriverPacket p = DriverPacket.getAckStable();
                    s.channel.sendPacket(p);
                    s.deepTransitionFlag.set(false);
                    next = new StableState(s);
                }
            }
            else{
                stablecount++;
            }
            //reset watchdog;
            watchdogcount = 0;
            onlyMain = false;
        }
        else{
            stablecount = 0;
            //watch dog!
            if(watchdogcount == WATCHDOGINTERVAL){
                synchronized (s.sLists){
                    Logger.log("watch dog");
                    for(ThreadTracker tracker: s.trackers){
                        Logger.log("thread " + tracker.tid + " is alive = " + tracker.thread.isAlive());
                        if(tracker.isActive && !tracker.thread.isAlive()){
                            Logger.log("Hanging Thread Detected! : (tid = " + tracker.tid + ")");
                            tracker.isActive = false;
                            s.entryCounter.decrementAndGet();
                        }
                    }

                    if(s.entryCounter.get() == 0 && s.activityCount.get() == (s.stopActivityCount.get() + s.pausedActivityCount.get())){
                        //This is the case when intent invoked something outside. We consider application is stopped.
                        if(onlyMain){
                            DriverPacket p = DriverPacket.getAckStop();
                            s.channel.sendPacket(p);
                            next = new StableState(s);
                        }
                        else{
                            onlyMain = true;
                        }
                    }
                    else{
                        onlyMain = false;
                    }
                }
                watchdogcount = 0;
            }
            else if(s.activityCount.get() == 0){
                synchronized (s.sLists){
                    Logger.log("everything paused for a while?");
                    s.printActivityStates();
                }
            }
            else{
                watchdogcount++;
            }
        }
    }

    public void checkAndHandleUnactivated(){
        long currentTime = System.currentTimeMillis();

        boolean flag1 = currentTime - startTime > s.TRANSITON_TIMEOUT;
        if(flag1 || (s.getCurrentActivity() == null && s.activityCount.get() == s.stopActivityCount.get())){
            Log.d("wtchoi", "stopstop");

            DriverPacket p;
            if(s.viweHelper.getViewRoots().length == 0) p = DriverPacket.getAckStop();
            else p = DriverPacket.getAckBlocked();

            s.channel.sendPacket(p);
            next = new StableState(s);
            //try{
            //    Thread.sleep(100);
            //}
            //catch(InterruptedException e){}
            //s.closeApplication();
        }
    }

    public synchronized AbstractState next(){ return next; }
}

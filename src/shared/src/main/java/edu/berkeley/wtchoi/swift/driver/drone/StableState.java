package edu.berkeley.wtchoi.swift.driver.drone;

import android.util.Log;
import edu.berkeley.wtchoi.swift.driver.DriverPacket;
import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;
import edu.berkeley.wtchoi.swift.driver.ViewComponentInfo;
import edu.berkeley.wtchoi.swift.driver.ViewInfo;
import edu.berkeley.wtchoi.logger.Logger;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 5:54 PM
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

public class StableState extends AbstractState {
    private AbstractState next;

    public StableState(SupervisorImp s){
        super(s);
        next = this;
    }

    public void work(){
        //get command packet
        DriverPacket packet = s.channel.receivePacketIgnoreTimeout();
        //Log.d("wtchoi", "packet received:" + packet.getType().toString());

        Log.d("wtchoi", "Stable State : " + packet.getType());

        //handle packet

        switch(packet.getType()){
            case PrepareCommand:
                handlePrepareCommand();
                DriverPacket p = DriverPacket.getAck();
                s.channel.sendPacket(p);
                break;

            case AckCommand:
                UnstableState uState = new UnstableState(s);
                if(packet.getExtra() != null){
                    try{
                        JSONObject json = new JSONObject((String)packet.getExtra());
                        if(json.has("stableCountGoal"))
                            uState.stableCountGoal = json.getInt("stableCountGoal");
                        if(json.has("initialSleep"))
                            uState.initialSleep = json.getInt("initialSleep");
                    }
                    catch(Exception ignore){}
                }
                next = uState;
                break;

            case RequestView:
            {
                long t = System.currentTimeMillis();
                s.channel.sendPacket(DriverPacket.getViewInfo(handleRequestView()));
                Logger.log("ViewComponentInfo transmitted:" + (System.currentTimeMillis() - t) + "ms");
                break;
            }
            case RequestCompressedLog:
            {
                long t = System.currentTimeMillis();
                s.channel.sendPacket(DriverPacket.getCompressedLog(handleRequestCompressedLog()));
                Logger.log("CompressedLog transmitted:" + (System.currentTimeMillis() - t) + "ms");
                break;
            }
            case RequestCoverage:
            {
                long t = System.currentTimeMillis();
                s.channel.sendPacket(DriverPacket.getCoverage(handleRequestCoverage()));
                Logger.log("Coverage transmitted:" + (System.currentTimeMillis() - t) + "ms");
                break;
            }
            case MultipleRequests:
            {
                long t = System.currentTimeMillis();
                handleMultipleRequest((List<DriverPacket.Type>) packet.getExtra());
                Logger.log("Results transmitted:" + (System.currentTimeMillis() - t) + "ms");
                break;
            }

            case Reset:
            {
                Logger.log("Application termination requested");
                s.closeApplication();
                break;
            }

            case SetOptions:
                int[] options  = packet.getExtraAs(int[].class);
                s.TICKCOUNT    = options[DriverPacket.OptionIndex.ITickCount.ordinal()];
                s.TICKINTERVAL = options[DriverPacket.OptionIndex.ITickInterval.ordinal()];
                s.TICKSNOOZE   = options[DriverPacket.OptionIndex.ITickSnooze.ordinal()];
                s.STABLECOUNT  = options[DriverPacket.OptionIndex.IStableCount.ordinal()];
                s.CHANNEL_TIMEOUT = options[DriverPacket.OptionIndex.IChannelTimeout.ordinal()];
                s.TRANSITON_TIMEOUT = options[DriverPacket.OptionIndex.ITransitionTimeout.ordinal()];
                s.channel.setTimeout(s.CHANNEL_TIMEOUT);
                s.channel.sendPacket(DriverPacket.getAck());
                break;

            case ClearData:
                s.clearData();
                s.channel.sendPacket(DriverPacket.getAck());
                break;

            case StartTraceLogging:
                s.flagTraceLogging = true;
                s.channel.sendPacket(DriverPacket.getAck());
                break;

            default:
                throw new RuntimeException("Wrong Packet:" + packet.getType().toString());
        }

        if(postPrepare){
            postPrepare = false;
            handlePrepareCommand();
        }
    }

    private void handleMultipleRequest(List<DriverPacket.Type> requests){
        LinkedList<Object> resultVector = new LinkedList<Object>();
        for(DriverPacket.Type r:requests){
            handleRequest(r, resultVector);
        }
        s.channel.sendPacket(DriverPacket.getMultipleResults(resultVector));
    }

    private void handleRequest(DriverPacket.Type r, LinkedList<Object> rv){
        switch(r){
            case RequestView:
                rv.add(handleRequestView());
                break;
            case RequestCompressedLog:
                rv.add(handleRequestCompressedLog());
                break;
            case RequestCoverage:
                rv.add(handleRequestCoverage());
                break;
            case PrepareCommand:
                //instead of handling it directly, delay it until all results are collected
                postPrepare = true;
                break;
        }
    }

    private boolean postPrepare = false;

    private void handlePrepareCommand(){
        Logger.log("HANDLE PREPARE COMMAND");
        synchronized (s.sLists){
            s.collectGarbageTracker();
            s.clearTrace(s.mainTid);
        }
    }

    private ViewInfo handleRequestView(){
        Logger.log("HANDLE REQUEST VIEW");
        //return  getAllViewInfo();
        Logger.log("====== Current Views =======");
        //int i = 1;
        //for(ViewComponentInfo view:s.viweHelper.getAllDecorViews()){
        //    Logger.log("ViewRoot #" + (i++));
        //    Logger.log(view.toString());
        //}
        ViewComponentInfo vr = s.viweHelper.getRecentDecorViewInfo();
        ViewInfo vi = new ViewInfo();
        vi.setViewRoot(vr);

        if(s.isInputMethodActive()) vi.setInputMethodActive();
        if(s.isInputMEthodFullScreen()) vi.setInputMethodFullScreen();
        return vi;
    }



    private CompressedLog handleRequestCompressedLog(){
        Logger.log("HANDLE REQUEST LOG");
        synchronized (s.sLists){
            return  s.getTrace(s.mainTid);
        }
    }

    private ProgramPointSet handleRequestCoverage(){
        Logger.log("HANDLE REQUEST COVERAGE");
        synchronized (s.sLists){
            ProgramPointSet pps = new ProgramPointSet();
            for(ThreadTracker tracker:s.trackers){
                pps.addAll(tracker.myDecisionPoints);
            }
            return pps;
        }
    }

    public synchronized AbstractState next(){ return next; }
}

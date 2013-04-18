package edu.berkeley.wtchoi.swift.driver;

import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 8:03 PM
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
public class DriverPacket implements Serializable{

    private static final long serialVersionUID = -5186309675577891457L;

    private static int id_next = 0;
    private int id;
    private Type type;
    private Object piggyback;

    private DriverPacket(Type t) {
        id = id_next++;
        type = t;
        piggyback = null;
    }

    private DriverPacket(Type t, Object back){
        id = id_next++;
        type = t;
        piggyback = back;
    }


    public static DriverPacket getAck() {
        return new DriverPacket(Type.Ack);
    }

    public static DriverPacket getReset() {
        return new DriverPacket(Type.Reset);
    }

    public static DriverPacket getRequestView() {
        return new DriverPacket(Type.RequestView);
    }

    public static DriverPacket getRequestCompressedLog(){
        return new DriverPacket(Type.RequestCompressedLog);
    }

    public static DriverPacket getRequestCoverage(){
        return new DriverPacket(Type.RequestCoverage);
    }

    public static DriverPacket getMultipleRequest(LinkedList<Type> requests){
        return new DriverPacket(Type.MultipleRequests, requests);
    }


    public static DriverPacket getAckCommand() {
        return new DriverPacket(Type.AckCommand);
    }

    public static DriverPacket getPrepareCommand(){
        return new DriverPacket(Type.PrepareCommand);
    }

    public static DriverPacket getClearData(){
        return new DriverPacket(Type.ClearData);
    }

    public static DriverPacket getAckStable() {
        return new DriverPacket(Type.AckStable);
    }

    public static DriverPacket getAckStop(){
        return new DriverPacket(Type.AckStop);
    }

    public static DriverPacket getAckBlocked(){
        return new DriverPacket(Type.AckBlocked);
    }

    public static DriverPacket getInitReport(Object report){
        return new DriverPacket(Type.InitialReport, report);
    }

    public static DriverPacket getSetOptions(int[] opt){
        return new DriverPacket(Type.SetOptions, opt);
    }

    public static DriverPacket getViewInfo(ViewInfo mv){
        return new DriverPacket(Type.ViewInfo, mv);
    }

    public static DriverPacket getCompressedLog(CompressedLog cl){
        return new DriverPacket(Type.CL, cl);
    }

    public static DriverPacket getCoverage(ProgramPointSet ps){
        return new DriverPacket(Type.Coverage, ps);
    }

    public static DriverPacket getMultipleResults(LinkedList<Object> results){
        return new DriverPacket(Type.MultipleResults, results);
    }

    public static DriverPacket getTruncatedTransitionInfo(int[] buffer, int size){
        Object[] obj = new Object[2];
        obj[0] = buffer;
        obj[1] = size;
        return new DriverPacket(Type.TTI, obj);
    }

    public static DriverPacket getEnterEditText(int vid, String content){
        Object[] obj = new Object[2];
        obj[0] = vid;
        obj[1] = content;
        return new DriverPacket(Type.EnterEditText, obj);
    }

    public static DriverPacket getStartTraceLogging(){
        return new DriverPacket(Type.StartTraceLogging);
    }

    public static DriverPacket getAckCrash(boolean usermode){
        return new DriverPacket(Type.AckCrash, new Boolean(usermode));
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public void assertType(Type type){
        if(this.getType() == type) return;
        throw new RuntimeException("Wrong DriverPacket! : expected=" + type + " , received=" + this.getType());
    }

    public Object getExtra(){
        return this.piggyback;
    }

    public <T> T getExtraAs(Class<T> classT){
        if(this.piggyback.getClass().isAssignableFrom(classT))
            return (T) this.piggyback;

        throw new RuntimeException("Wrong Type! : expected=" + classT + " , received=" + this.piggyback.getClass());
    }

    public void setExtra(Object extra){
        this.piggyback = extra;
    }

    public static enum OptionIndex{
        ITickCount,
        IStableCount,
        ITickInterval,
        ITickSnooze,
        IChannelTimeout,
        ITransitionTimeout;
    }


    public static enum Type {
        Ack("Ack"),

        PrepareCommand("PrepareCommand"),
        AckCommand("AckCommand"),
        RequestView("RequestView"),
        RequestCompressedLog("RequestCompressedLog"),
        RequestCoverage("RequestCoverage"),
        MultipleRequests("MultipleRequests"),
        SetOptions("SetOptions"),
        Reset("Reset"),
        EnterEditText("EnterEditText"),
        ClearData("ClearData"),
        StartTraceLogging("StartTraceLogging"),

        // DriverPacket from Application
        InitialReport("InitialReport"),
        AckStable("AckStable"),
        ViewInfo("ViewInfo"),
        CL("CompressedLog"),
        Coverage("Coverage"),
        TTI("TruncatedTransitionInfo"),
        MultipleResults("MultipleResults"),
        AckStop("AckStop"),
        AckBlocked("AckBlocked"),
        AckCrash("AckCarsh");

        final String name;

        private Type(String string){
            this.name = string;
        }

        @Override
        public String toString(){
            return name;
        }

    }

}

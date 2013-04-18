package edu.berkeley.wtchoi.swift.driver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.berkeley.wtchoi.collection.ExtendedComparable;

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
public abstract class ICommand implements ExtendedComparable<ICommand> {

    private Object extra;
    private String widgetType;
    private int widgetTypeHash;

    public void sendCommand(Driver driver) throws Device.CannotSendCommand, ApplicationTerminated, ApplicationCrash, InstrumentationCrash{
        sendCommandPrepare(driver);
        sendCommandImp(driver);
        sendCommandAck(driver);
    }

    protected void sendCommandImp(Driver driver) throws Device.CannotSendCommand, ApplicationTerminated{
        throw new RuntimeException("not implemented");
    }

    public abstract Integer typeint();
    //This is for fast comparison between different implementation of ICommand interface
    //All different implementation should use different Integer number;

    //All implementation of command should obtain integer identifier from
    //private static final Integer tint = IdentifierPool.getFreshInteger();

    //Please call this function before sending chimpchat command
    protected void sendCommandPrepare(Driver driver){
        /*
        DriverPacket prepare = DriverPacket.getPrepareCommand();
        driver.channel.sendPacket(prepare);

        DriverPacket received = driver.channel.receivePacket();
        received.assertType(DriverPacket.Type.Ack);
        */
        //now piggy backed to information query
    }

    private static Gson gson = new Gson();

    //Please call this function after sending chimpchat command
    protected void sendCommandAck(Driver driver) throws Device.CannotSendCommand, ApplicationTerminated, ApplicationCrash, InstrumentationCrash{
        //1. Send command acknowledgement to App Supervisor
        DriverPacket ack = DriverPacket.getAckCommand();


        JsonObject obj = new JsonObject();
        if(getStableCountGoal() != null)
            obj.addProperty("getStableCountGoal", getStableCountGoal());
        if(getInitialSleep(driver) != null)
            obj.addProperty("initialSleep", getInitialSleep(driver));

        ack.setExtra(obj.toString());
        driver.channel.sendPacket(ack);

        //2. Minor sleep
        //E.sleep(100);

        //3. Wait for App Supervisor response
        driver.waitApplicationToBeStable();
    }

    public int compareTo(ICommand target){
        int ti1 = typeint();
        int ti2 = target.typeint();
        if(ti1 > ti2) return 1;
        else if (ti1 < ti2) return -1;

        ti1 = widgetTypeHash;
        ti2 = target.widgetTypeHash;
        if(ti1 > ti2) return 1;
        else if (ti1 < ti2) return -1;

        return compareSameType(target);
    }

    public boolean equalsTo(ICommand target){
        if(typeint().compareTo(target.typeint()) == 0) return false;
        if(widgetTypeHash != target.widgetTypeHash) return false;
        return equalsToSameType(target);
    }

    protected abstract int compareSameType(ICommand target);
    protected abstract boolean equalsToSameType(ICommand target);

    protected Integer getStableCountGoal(){
        return null;
    }

    protected Integer getInitialSleep(Driver driver){
        return null;
    }

    public void setWidgetType(String typename){
        widgetType = typename;
        widgetTypeHash = typename.hashCode();
    }

    public String getWidgetType(){
        return widgetType;
    }

    public boolean isManual(){
        return false;
    }
}

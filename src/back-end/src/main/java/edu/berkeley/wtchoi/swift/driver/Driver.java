package edu.berkeley.wtchoi.swift.driver;

import edu.berkeley.wtchoi.swift.driver.DriverPacket.OptionIndex;
import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;
import edu.berkeley.wtchoi.swift.testing.android.lstar.TransitionInfo;
import edu.berkeley.wtchoi.swift.util.E;
import edu.berkeley.wtchoi.util.TcpChannel;

import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

public class Driver{
   
    DriverOption option;
        
    //private ChimpChat mChimpchat;

    //IChimpDevice mDevice;
    Device device;
    TcpChannel<DriverPacket> channel;

    final static int StateNotInstalled = 1;  //Application is not installed
    final static int StateJustInstalled = 2; //Application is installed and yet started
    final static int StateActive = 3; //Application is installed and activated
    final static int StateInactive = 4; //Application is installed and inactivate

    private int appState;
    private int localPort;
    private int observationInterval;

    private Driver(DriverOption option, Device device, int localPort) {
        super();
        this.option = option;
        this.device = device;
        this.localPort = localPort;
        appState = StateNotInstalled;
    }


    // Initiate application, connect chip, connect channel
    public static  Driver connectToDevice(DriverOption option, String deviceID, int localport){
        option.assertComplete();

        Device.init(option.getADB());
        Device device = Device.waitForConnection(option.getTimeout(), deviceID, localport, 13338);
        if (device == null) {
            //throw new RuntimeException("Couldn't connect.");
            return null;
        }
        System.out.println("Device Connected");
        device.wake();
        System.out.println("Device Waked");
        return new Driver(option,device,localport);
    }

    public static Driver getDriverWithDevice(DriverOption option, Device device, int localPort){
        return new Driver(option, device, localPort);
    }

    public Device getDevice(){
        return device;
    }

    public boolean cleanStartApp(){
        /*if(appState != StateActive){
            device.installPackage(option.getApplicationBinaryPath());
            if(initiateApp(false) == false)
                throw new RuntimeException("Cannot initialize application!");
        }

        channel.sendPacket(DriverPacket.getClearData());
        channel.receivePacketIgnoreTimeout().assertType(DriverPacket.Type.Ack);
        channel.sendPacket(DriverPacket.getReset());
        */
        device.removePackage(option.getApplicationPackage());
        device.installPackage(option.getApplicationBinaryPath());
        appState = StateJustInstalled;
        return true;
    }

    public void startApp(){
        if(appState != StateJustInstalled){
            cleanStartApp();
        }

        if(initiateApp(true) == false)
            throw new RuntimeException("Cannot initialize application!");
    }

    public void closeApplication(){
        removeApplication();
    }

    private boolean initiateApp(boolean sentOptions) {
        ////1. Initiate Communication TcpChannel (Asynchronous)
        //channel = TcpChannel.getServerSide(13338);
        //channel.connectAsynchronous();
        channel = TcpChannel.getClientSide("127.0.0.1", localPort);
        channel.setTryCount(5);
        channel.setTryInterval(1000);

        //1.5. Wait phone to clean up previously died-application instance
        long minimumWait = 2000;
        long elapsedTime = System.currentTimeMillis() - stopTimeStamp;
        if(elapsedTime < minimumWait){
            E.sleep(minimumWait - elapsedTime);
        }

        //2. Initiate ChimpChat connection
        String runComponent = option.getRunComponent();
        Collection<String> coll = new LinkedList<String>();
        Map<String, Object> extras = new HashMap<String, Object>();
        boolean successFlag = false;
        for(int i = 0; i<10 ; i++){
            System.out.println("send wake up!");
            successFlag = device.startActivity(null, null, null, null, coll, extras, runComponent, 0);
            E.sleep(100);
            if(successFlag)
                break;
        }


        //3. Wait for communication channel initiation
        System.out.println("wait");
        channel.connect();
        //channel.waitConnection();
        System.out.println("go");

        //3.5 Wait for the initial report
        {
            System.out.println("Waiting for initial information report");
            DriverPacket packet = channel.receivePacketIgnoreTimeout();
            packet.assertType(DriverPacket.Type.InitialReport);
        }


        System.out.println("Waiting for application to be stable");

        //4. Wait for application to be ready for command
        {
            try{
                waitApplicationToBeStable();
            }
            catch(ApplicationCrash e){
                throw new RuntimeException("Application Crashed!", e);
            }
            catch(InstrumentationCrash e){
                throw new RuntimeException("Instrumentation Filed!", e);
            }
            catch(Exception e){}

            appState = StateActive;
            System.out.println("Application Initiated");
        }


        //5. Setup testing parameters
        if(sentOptions){
            int[] opt = new int[OptionIndex.values().length];
            opt[OptionIndex.ITickInterval.ordinal()] = option.getTickInterval();
            opt[OptionIndex.ITickCount.ordinal()]    = option.getTickCount();
            opt[OptionIndex.ITickSnooze.ordinal()]   = option.getTickSnooze();
            opt[OptionIndex.IStableCount.ordinal()]  = option.getStableCount();
            opt[OptionIndex.IChannelTimeout.ordinal()] = option.getChannelTimeout();
            opt[OptionIndex.ITransitionTimeout.ordinal()] = option.getTransitionTimeout();


            DriverPacket packet = DriverPacket.getSetOptions(opt);
            channel.sendPacket(packet);
            channel.receivePacketIgnoreTimeout();

            System.out.println("Testing option sent");
        }

        return true;
        //NOTE: At this moment, we expect application to erase all user data when ever it starts.
        //Therefore, our protocol doesn't have anythings about resetting application data.
        //However, we may need more complex protocol to fine control an application.
    }

    public void waitApplicationToBeStable() throws ApplicationTerminated, ApplicationCrash, InstrumentationCrash{
        DriverPacket receivingPacket;
        try{
            receivingPacket = channel.receivePacket();
            switch(receivingPacket.getType()){
                case AckStop:{
                    throw new ApplicationTerminated();
                }
                case AckBlocked:{
                    //clearData();
                    //channel.sendPacket(DriverPacket.getReset());
                    throw new ApplicationTerminated();
                }
                case AckCrash:{
//                  channel.sendPacket(DriverPacket.getAck());
                    Boolean flag = (Boolean) receivingPacket.getExtra();
                    System.out.println("application crashed! : " + flag);
                    if(flag) throw new ApplicationCrash();
                    else throw new InstrumentationCrash();
                }
            }
        }
        catch(SocketTimeoutException e){ throw new ApplicationTerminated(); }
        catch(ApplicationTerminated e){ throw e;}
        catch(ApplicationCrash e){ throw e; }
        catch(InstrumentationCrash e){ throw e; }
        catch(Exception e){
            System.out.println("application crashed! other exception:" + e.toString());
            throw new InstrumentationCrash();
        }

        receivingPacket.assertType(DriverPacket.Type.AckStable);
        return;
    }

    /*
    public void clearData(){
        if(appState == STATE_INIT || appState == STATE_ON_CLEAN_REQUESTED) return;

        channel.sendPacket(DriverPacket.getClearData());
        DriverPacket received = channel.receivePacket();
        received.assertType(DriverPacket.Type.Ack);
        System.out.println("Data clear request sent");

        if(appState == STATE_ON_DIRTY)
            appState = STATE_ON_CLEAN_REQUESTED;
        else{
            throw new RuntimeException("Something is Wrong!");
        }

        return;
    }
    */

    public void removeApplication(){
        device.removePackage(option.getApplicationPackage());
        appState = StateNotInstalled;
    }

    public boolean restartApp(){
        if(appState != StateJustInstalled){
            System.out.println("RESTART: Restart by reinstall");
            cleanStartApp();
            if(initiateApp(true)){
                if(option.isRequestingTraceLogging()){
                    channel.sendPacket(DriverPacket.getStartTraceLogging());
                    DriverPacket packet = channel.receivePacketIgnoreTimeout();
                    packet.assertType(DriverPacket.Type.Ack);
                }
                return true;
            }
            return false;
        }
        System.out.println("RESTART: Application is already in initial state");
        return true;
    }


    public ViewComponentInfo getCurrentView() {
        ViewComponentInfo mv;

        DriverPacket sPacket = DriverPacket.getRequestView();
        channel.sendPacket(sPacket);

        DriverPacket rPacket = channel.receivePacketIgnoreTimeout();
        rPacket.assertType(DriverPacket.Type.ViewInfo);
        mv = rPacket.getExtraAs(ViewComponentInfo.class);

        //DEBUG PRINT : whether received information is correct or not
        //System.out.println(mv);

        return mv;
    }


    private TransitionInfo ti;
    public TransitionInfo getCurrentTransitionInfo(){

        DriverPacket sPacket = DriverPacket.getRequestCompressedLog();
        channel.sendPacket(sPacket);

        DriverPacket rPacket = channel.receivePacketIgnoreTimeout();
        rPacket.assertType(DriverPacket.Type.CL);

        CompressedLog trace = (CompressedLog) rPacket.getExtra();
        System.out.println("Log received = " + trace.size());
        TransitionInfo receivedTI = new TransitionInfo(trace);
        addTI(receivedTI);

        TransitionInfo tempTI = ti;
        ti = null;

        return tempTI;
    }

    public ProgramPointSet getCoverage(){
        DriverPacket toSend = DriverPacket.getRequestCoverage();
        channel.sendPacket(toSend);

        DriverPacket receive = channel.receivePacketIgnoreTimeout();
        receive.assertType(DriverPacket.Type.Coverage);

        ProgramPointSet result = (ProgramPointSet) receive.getExtra();

        return result;
    }

    public LinkedList<Object> getAll(boolean prepareFlag){

        LinkedList<DriverPacket.Type> requests = new LinkedList<DriverPacket.Type>();
        requests.add(DriverPacket.Type.RequestView);
        requests.add(DriverPacket.Type.RequestCompressedLog);
        requests.add(DriverPacket.Type.RequestCoverage);
        if(prepareFlag) requests.add(DriverPacket.Type.PrepareCommand);
        LinkedList<Object> result = requestMultiple(requests);

        return result;
    }


    public LinkedList<Object> requestMultiple(LinkedList<DriverPacket.Type> requests){
        DriverPacket toSend = DriverPacket.getMultipleRequest(requests);
        channel.sendPacket(toSend);

        DriverPacket received = channel.receivePacketIgnoreTimeout();
        received.assertType(DriverPacket.Type.MultipleResults);

        LinkedList<Object> result = (LinkedList<Object>) received.getExtra();
        return result;
    }

    public void addTI(TransitionInfo ti){
        if(this.ti == null) this.ti = ti;
        else this.ti.concat(ti);
    }

    private long stopTimeStamp = 0;

    public boolean go(ICommand c) {

        boolean result = false;
        try{
            c.sendCommand(this);
            result = device.errorCheck();
        }
        catch(ApplicationTerminated e){
            appState = StateInactive;
            stopTimeStamp = System.currentTimeMillis();

        }
        catch(ApplicationCrash e){
            appState = StateInactive;
            stopTimeStamp = System.currentTimeMillis();
        }
        catch(InstrumentationCrash ee){
            appState = StateInactive;
            stopTimeStamp = System.currentTimeMillis();
            //throw new RuntimeException("Instrumentation Failed!", ee);
        }
        catch(Device.CannotSendCommand ee){
            appState = StateInactive;
            stopTimeStamp = System.currentTimeMillis();
            throw new RuntimeException("Crash!", ee);
        }

        return result;
    }

    public void setChannelTimeout(int msec){
        option.setChannelTimeout(msec);
    }
}

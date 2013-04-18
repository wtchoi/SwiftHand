package edu.berkeley.wtchoi.swift.driver;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import edu.berkeley.wtchoi.swift.util.E;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/19/12
 * Time: 10:22 PM
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

//Device class wraps AdbChimpChat class. Two main purposes:
//1. to provide interface with success/failure return
//2. to connect device with specific port forwarding

public class Device{
    public static class CannotSendCommand extends Exception{}

    private static TreeMap<Long, LinkedList<String>> logMap;
    private static ChimpChat mChimpChat;
    private static AndroidDebugBridge bridge;

    public static void init(String adbPath){
        logMap = new TreeMap<Long, LinkedList<String>>();
        Logger LOG = Logger.getLogger(AdbChimpDevice.class.getName());
        LOG.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                long tid = Thread.currentThread().getId();
                if(!logMap.containsKey(tid))
                    logMap.put(tid,new LinkedList<String>());
                logMap.get(tid).add(record.getMessage());
            }

            @Override
            public void flush() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void close() throws SecurityException {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        TreeMap<String, String> options = new TreeMap<String, String>();
        options.put("backend", "adb");
        options.put("adbLocation", adbPath);

        mChimpChat = ChimpChat.getInstance(options);
        edu.berkeley.wtchoi.logger.Logger.log("get ChimpChat instance");


        bridge = AndroidDebugBridge.getBridge();
        if(bridge == null)
            throw new RuntimeException("Cannot get ADB");
        else
            edu.berkeley.wtchoi.logger.Logger.log("get ADB");
    }

    public void disconnect(){
        //mChimpChat.shutdown();
        //mChimpDevice.dispose();
    }


    private IChimpDevice mChimpDevice;
    private IDevice mIDevice;

    private Device(IChimpDevice cd, IDevice id){
        mChimpDevice = cd;
        mIDevice = id;
    }



    private static FileLock lock;
    private static void acquireLock(){
        try{
            File file = new File("/tmp/wtchoi.swift.lock");
            file.createNewFile();
            FileOutputStream fi = new FileOutputStream(file);
            FileChannel fc = fi.getChannel();
            lock = fc.lock();
        }
        catch(Exception e){
            throw new RuntimeException("Cannot acquire file lock", e);
        }
    }

    private static void releaseLock(){
        try{
            lock.release();
            lock = null;
        }
        catch(Exception e){
            throw new RuntimeException("Cannot release lock", e);
        }
    }


    public static Device waitForConnection(final long timeout, final String identifier, final int localport, final int remortport){
        Pattern pattern = Pattern.compile(identifier);

        while(!bridge.isConnected() || !bridge.hasInitialDeviceList()){
            E.sleep(200);
        }

        IDevice target = null;
        for(IDevice device : bridge.getDevices()){
            String serialNumber = device.getSerialNumber();
            if(pattern.matcher(serialNumber).matches()){
                    target = device;
                    break;
            }
        }
        if(target == null){
            throw new RuntimeException("Cannot find device!");
        }


        IChimpDevice __device;
        //acquireLock();
        {
            try{
                target.createForward(localport, remortport);
            }
            catch(Exception e){
                e.printStackTrace();
                throw new RuntimeException("Cannot make port forwarding!");
            }
            __device = mChimpChat.waitForConnection(timeout, identifier);
        }
        //releaseLock();

        if(__device == null) return null;
        return new Device(__device,target);
    }

    public void installPackage(final String filePath){
        try{
            mChimpDevice.installPackage(filePath);
        }
        catch(Exception e){
            if(e instanceof InstallException){
                E.sleep(1000);
                mChimpDevice.installPackage(filePath);
            }
            else{
                throw new RuntimeException(e);
            }
        }
    }

    public void removePackage(final String packageName){
        mChimpDevice.removePackage(packageName);
    }

    public boolean isEmulator(){
        return mIDevice.isEmulator();
    }

    public boolean errorCheck(){
        LinkedList<String> log = pollLog();
        if(log.size() != 0) return false;
        return true;
    }

    public void touch(int x, int y, TouchPressType type){
        mChimpDevice.touch(x, y, type);
    }

    public void longTouch(int x, int y){
        mChimpDevice.touch(x, y, TouchPressType.DOWN);
        try{Thread.sleep(3000);}catch(Exception ignore){}
        mChimpDevice.touch(x, y, TouchPressType.UP);
        //mChimpDevice.drag(x,y,x,y,3,1);
    }

    public void press(PhysicalButton button, TouchPressType type){
        mChimpDevice.press(button, type);
    }

    public void type(String string){
        mChimpDevice.type(string);
    }

    public void press(String key, TouchPressType type){
        mChimpDevice.press(key, type);
    }

    public void drag(int x_from, int y_from, int x_to, int y_to){
        mChimpDevice.drag(x_from, y_from, x_to, y_to, 10, 500);
    }

    public void wake(){
        mChimpDevice.wake();
    }

    public boolean startActivity(String s1, String s2, String s3, String s4, Collection<String> opt1, Map<String, Object> opt2, String s5, int i){
        mChimpDevice.startActivity(s1, s2, s3, s4, opt1, opt2, s5, i);
        return errorCheck();
    }

    private static LinkedList<String> pollLog(){
        long tid = Thread.currentThread().getId();
        LinkedList<String> log = logMap.get(tid);
        logMap.put(tid,new LinkedList<String>());
        return (log == null)? (new LinkedList<String>()) : log;
    }
}

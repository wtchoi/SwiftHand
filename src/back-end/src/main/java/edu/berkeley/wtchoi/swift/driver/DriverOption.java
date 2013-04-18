package edu.berkeley.wtchoi.swift.driver;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/26/12
 * Time: 8:46 PM
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

public class DriverOption {
    private String mainActivity;
    private String applicationPackage;
    private long   timeout = 5000;
    private String adb;

    private int mTickCount = 2;
    private int mTickInterval = 100;
    private int mTickSnooze = 1;
    private int mStableCount = 3;
    private int mChannelTimeout = 1000000;
    private int mTransitionTimeout = 30000;

    private boolean mTraceLogging = true;

    public void getAdbPathFromEnvironment(){
        adb = System.getenv("ADK_ROOT") + "/platform-tools/adb";
    }

    //to check whether all basic information is there
    public boolean isComplete(){
        if(mainActivity == null) return false;
        if(applicationPackage == null) return false;
        if(adb == null) return false;
        return true;
    }

    //to check and raise exception
    public void assertComplete(){
        if(mainActivity == null) throw new RuntimeException(__msg1);
        if(applicationPackage == null) throw new RuntimeException(__msg2);
        if(adb == null) throw new RuntimeException(__msg3);
    }
    private static String __msg1 = "Main Activity is not specified";
    private static String __msg2 = "Application Package is not specified";
    private static String __msg3 = "ADB path is not specified";


    //get methods
    public String getApplicationPackage(){
        return applicationPackage;
    }

    public String getRunComponent(){
        return applicationPackage + '/' + applicationPackage + "." + mainActivity;
    }

    public String getADB(){
        return adb;
    }

    public long getTimeout(){
        return timeout;
    }

    public int getTickCount(){
        return mTickCount;
    }

    public int getTickInterval(){
        return mTickInterval;
    }

    public int getTickSnooze(){
        return mTickSnooze;
    }

    public int getStableCount(){
        return mStableCount;
    }


    //set methods
    public void setMainActivity(String s){
        mainActivity = s;
    }

    public void setApplicationPackage(String s){
        applicationPackage = s;
    }

    public void setTimeout(long t){
        timeout = t;
    }

    public void setADB(String s){
        adb = s;
    }

    public void setTraceLogging(boolean t){
        mTraceLogging = t;
    }

    public boolean isRequestingTraceLogging(){
        return mTraceLogging;
    }

    public String getApplicationBinaryPath(){
        return applicationBinary;
    }

    String applicationBinary = null;
    public void setApplicationBinary(String path){
        applicationBinary = path;
    }

    public void setChannelTimeout(int msec){
        mChannelTimeout = msec;
    }

    public int getChannelTimeout(){
        return mChannelTimeout;
    }

    public void setDeviceObservationTickInterval(int msec){
        mTickInterval = msec;
    }

    public void setTransitionTimeout(int msec){
        mTransitionTimeout = msec;
    }

    public int getTransitionTimeout(){
        return mTransitionTimeout;
    }
}

package edu.berkeley.wtchoi.swift.driver.drone;
//INSTRUMENTATION

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import edu.berkeley.wtchoi.logger.Logger;
import edu.berkeley.wtchoi.logger.LoggerImp;

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
public abstract class Supervisor{
	 
	private static SupervisorImp supervisor;

    private static LoggerImp logger = new LoggerImp(){
        @Override
        public void log(String s){
            Log.d("wtchoi(Logger)", s);
        };
    };

    private final static int ModeUninitialized = 1;
    private final static int ModePrepareCalled = 2;
    private final static int ModeAppRegistered = 3;
    private final static int ModeInitialized = 4;

    private static int mSupervisorMode = ModeUninitialized;
    private static boolean mVerboseMode = false;

    public static void appPrepare(){
        //If this is first execution of application,
        //initialize supervisor
        if(mSupervisorMode == ModeUninitialized){
            Logger.init(logger);
            supervisor = new SupervisorImp();
            supervisor.prepare();
            mSupervisorMode = ModePrepareCalled;
        }
    }

    public static void appRegisterApplication(Application app){
        if(mSupervisorMode == ModePrepareCalled){
            supervisor.registerApplication(app);
            mSupervisorMode = ModeAppRegistered;
        }
    }

    private static void tryRegisterApplicationFromActivity(Activity activity){
            if(mSupervisorMode == ModePrepareCalled){
                supervisor.registerApplication(activity.getApplication());
                mSupervisorMode = ModeAppRegistered;
            }
    }

    public static void appStart(){
        if(mSupervisorMode == ModeAppRegistered){
            start();
            mSupervisorMode = ModeInitialized;
        }
    }


    private static void clearData(){
        supervisor.clearData();
    }

	private static void start(){
        Logger.log("Supervisor Start");
		supervisor.start();
	}

    public static void logEnter(short fid) throws Throwable{
        supervisor.logEnter(fid);
    }

    public static void logEnterCLINIT(short fid) throws Throwable{
        supervisor.logEnterCLINIT(fid);
        //Logger.log("ENTER_CLINIT: "+ fid);
    }

    public static void logExit(short fid) throws Throwable{
        supervisor.logExit(fid);
        //Logger.log("EXIT: " + fid);
    }


    public static void logCall(short fid) throws Throwable{
        supervisor.logCall(fid);
        //Logger.log("CALL: "+ fid);
	}

	
	public static void logReturn(short fid) throws Throwable{
        supervisor.logReturn(fid);
        //Logger.log("RETURN: "+ fid);
	}

    public static void logUnroll(short fid, Exception e) throws Throwable{
        supervisor.logUnroll(fid, e);
        //Logger.log("UNROLL_CATCH: "+ fid);
    }

    public static void logCatch(short fid) throws Throwable{
        supervisor.logCatch(fid);
        //Logger.log("CATCh: "+ fid);
    }

    public static void logThrow(short fid) throws Throwable{
        supervisor.logThrow(fid);
        //Logger.log("THROW" + fid);
    }

    public static void logReceiver(Object o, short fid) throws  Throwable{
        supervisor.logReceiver(o,fid);
    }

    public static void logProgramPoint(int ppid, short fid) throws Throwable{
        supervisor.logProgramPoint(ppid, fid);
        //Logger.log("PP " + fid + " " + ppid);
    }

    public static void logProgramPointExtra(int ppid, short fid) throws Throwable{
        supervisor.logProgramPointExtra(ppid, fid);
        //Logger.log("PP " + fid + " " + ppid);
    }

    public static void logDecisionPoint(int ppid, short fid){
        supervisor.logDecisionPoint(ppid, fid);
    }

	public static void logActivityCreatedEnter(Activity a) throws Throwable{
        Logger.log("Activity Create Enter");
        //tryRegisterApplicationFromActivity(a);
        supervisor.logActivityCreatedEnter(a);
	}

    public static void logActivityCreatedExit(Activity a) throws Throwable{
        Logger.log("Activity(" + a.toString() + ") is Create Exit");
        supervisor.logActivityCreatedExit(a);
    }

    public static void logResumeEnter(Activity a) throws Throwable{
        supervisor.logResumeEnter(a);
    }

    public static void logResumeExit(Activity a) throws Throwable{
        supervisor.logResumeExit(a);
    }
	
	public static void logStartEnter(Activity a) throws Throwable{
        //Logger.log("Activity Start Enter");
        //appStart();
        supervisor.logStartEnter(a);
	}

    public static void logStartExit(Activity a) throws Throwable{
        Logger.log("Activity(" + a.toString() + ") is Start Exit");
        supervisor.logStartExit(a);
    }

    public static void logPauseEnter(Activity a) throws Throwable{
        supervisor.logPauseEnter(a);
    }

    public static void logPauseExit(Activity a) throws Exception{
        Logger.log("Activity(" + a.toString() + ") is Paused");
        supervisor.logPauseExit(a);
    }

    public static void logStopEnter(Activity a) throws Throwable{
        supervisor.logStopEnter(a);
	}

    public static void logStopExit(Activity a) throws Exception{
        Logger.log("Activity(" + a.toString() + ") is Stoped");
        supervisor.logStopExit(a);
    }

    public static void logDestroyEnter(Activity a) throws Exception{
        supervisor.logDestroyEnter(a);
    }

    public static void logDestroyExit(Activity a) throws Exception{
        Logger.log("Activity(" + a.toString() + ") is Destroyed");
        supervisor.logDestroyExit(a);
    }
}

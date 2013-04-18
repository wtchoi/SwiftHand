package edu.berkeley.wtchoi.swift.driver.drone;
//INSTRUMENTATION

import android.app.Application;
import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

import java.io.File;

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
public class ApplicationWrapper {
    private Application app;
    
    public ApplicationWrapper(Application a){
        app = a;   
    }

    //reference: http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    public void hideKeyboard(IBinder windowToken){
        InputMethodManager imm = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    public boolean inputMethodActive(){
        InputMethodManager imm = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();
    }

    public boolean inputMethodFullScreen(){
        InputMethodManager imm = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isFullscreenMode();
    }

    //SNIPPET from  http://www.hrupin.com/2011/11/how-to-clear-user-data-in-your-android-application-programmatically
    //Removing Application Data
    public void clearData(){
        File cache = app.getCacheDir();
        File appDir = new File(cache.getParent());
        if(appDir.exists()){
            String[] children = appDir.list();
            for(String s: children){
                if(!s.equals("lib")){
                    deleteDir(new File(appDir,s));
                }
            }
        }
    }
    
    private static boolean deleteDir(File dir){
        if(dir != null && dir.isDirectory()){
            String[] children = dir.list();
            for(int i=0; i< children.length; i++){
                boolean success = deleteDir(new File(dir, children[i]));
                if(!success){
                    return false;
                }
            }
        }
        return dir.delete();
    }
}

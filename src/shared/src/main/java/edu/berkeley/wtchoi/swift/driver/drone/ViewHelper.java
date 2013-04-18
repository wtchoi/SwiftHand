package edu.berkeley.wtchoi.swift.driver.drone;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import edu.berkeley.wtchoi.swift.driver.ViewComponentInfo;
import edu.berkeley.wtchoi.logger.Logger;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/8/13
 * Time: 5:53 PM
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
public class ViewHelper {
    private SupervisorImp s;

    public ViewHelper(SupervisorImp s){
        this.s = s;
    }

    public View getRecentDecorView(){
        try{
            View[] views = getViewRoots();
            if(views == null) return null;

            long drawingTime = 0;
            long drawingTimeWindowFocused = 0;
            View recent = null;
            View recentWindowFocused = null;

            //code snipet from ROBOTIUM
            for(int j=0;j<views.length;j++){
                View view = views[j];
                if(view != null && view.isShown()  /*&& view.hasWindowFocus()*/ && view.getDrawingTime() > drawingTime){
                    recent = view;
                    drawingTime = recent.getDrawingTime();
                }
                if(view != null && view.isShown()  && view.hasWindowFocus() && view.getDrawingTime() > drawingTimeWindowFocused){
                    recentWindowFocused = view;
                    drawingTimeWindowFocused = recentWindowFocused.getDrawingTime();
                }
            }
            if(recentWindowFocused != null) return recentWindowFocused;
            if(recent == null) return null;
            return recent;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot access view information");
        }
    }

    private static MessageDigest messageDigest;
    private static ByteBuffer messageBuffer;
    {
        try{
            messageDigest = MessageDigest.getInstance("MD5");
            messageBuffer = ByteBuffer.allocate(16);
            messageBuffer.mark();
        }
        catch(NoSuchAlgorithmException ignore){}
    }

    public byte[] getRecentDecorViewMD5(){
        View view = getRecentDecorView();
        if(view == null) return null;

        try{
            messageDigest.reset();
            collectCoordinates(view);
            byte[] digest = messageDigest.digest();
            messageDigest.reset();
            return digest;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void collectCoordinates(View view){
        messageBuffer.reset();
        messageBuffer.putInt(view.getLeft());
        messageBuffer.putInt(view.getTop());
        messageBuffer.putInt(view.getWidth());
        messageBuffer.putInt(view.getHeight());
        messageDigest.update(messageBuffer.array());

        if(view instanceof ViewGroup){
            ViewGroup vg = (ViewGroup)view;
            for(int i =0;i<vg.getChildCount(); i++){
                collectCoordinates(vg.getChildAt(i));
            }
        }
    }

    public ViewComponentInfo getRecentDecorViewInfo(){
        View recentDecorView = getRecentDecorView();
        while(recentDecorView == null){
            try{
                Logger.log("wait for decor view");
                Thread.sleep(100);
                recentDecorView = getRecentDecorView();
            }
            catch(Exception ignore){}
        }
        return ViewTransformer.fromView(recentDecorView);
    }

    public ViewComponentInfo getAllViewInfo(){
        //View Hierarchy Analysis
        try{
            View[] views = getViewRoots();

            //sort decor view based on their drawing time
            Arrays.sort(views, new Comparator<View>() {
                public int compare(View v1, View v2) {
                    return Long.valueOf(v1.getDrawingTime()).compareTo(v2.getDrawingTime());
                }
            });

            LinkedList<ViewComponentInfo> vlist = new LinkedList<ViewComponentInfo>();

            for(int i=0 ; i<views.length ; i++){
                View v = views[i];
                //Log.d("wtchoi!","<<" + v.getWidth() + "," + v.getHeight() + ">>");
                vlist.addFirst(ViewTransformer.fromView(v));
            }

            //Embed view roots into virtual global view root
            ViewComponentInfo root = new ViewComponentInfo(0,0,s.getScreenX(),s.getScreenY(),s.getScreenX(), s.getScreenY(), vlist);

            return root;
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot access view information");
        }
    }

    public View getActivityViewRoot(Activity a){
        Window rootW = a.getWindow();
        while(true){
            if(rootW.getContainer() == null) break;
            rootW = rootW.getContainer();
        }
        return rootW.getDecorView();
    }

    public ViewComponentInfo[] getAllDecorViews(){
        View[] roots = getViewRoots();
        ViewComponentInfo[] vis = new ViewComponentInfo[roots.length];
        for(int i =0;i<roots.length;i++){
            vis[i] = ViewTransformer.fromView(roots[i]);
        }
        return vis;
    }



    private static boolean __initialized = false;
    private static Field instanceField;
    private static Field viewsField;


    public View[] getViewRoots()
            //throws ClassNotFoundException, SecurityException,
            //NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        //Code snippet from ROBOTIUM

        try{
            if(!__initialized){
                String windowManagerString;
                String windowManagerImpString;
                if(android.os.Build.VERSION.SDK_INT >= 17){
                    windowManagerString = "sDefaultWindowManager";
                }
                else if(android.os.Build.VERSION.SDK_INT >= 13)
                    windowManagerString = "sWindowManager";
                else
                    windowManagerString = "mWindowManager";


                if(Build.VERSION.SDK_INT >= 17){
                    windowManagerImpString = "android.view.WindowManagerGlobal";
                }
                else{
                    windowManagerImpString = "android.view.WindowManagerImpl";
                }

                Class<?> windowManager = Class.forName(windowManagerImpString);
                ViewHelper.viewsField = windowManager.getDeclaredField("mViews");
                ViewHelper.instanceField = windowManager.getDeclaredField(windowManagerString);

                ViewHelper.viewsField.setAccessible(true);
                ViewHelper.instanceField.setAccessible(true);
                ViewHelper.__initialized = true;
            }

            Object instance = ViewHelper.instanceField.get(null);
            return (View[]) ViewHelper.viewsField.get(instance);
        }
        catch(Throwable e){
            throw new RuntimeException("Cannot get view information", e);
        }
    }

}

package edu.berkeley.wtchoi.swift.driver;

import com.android.chimpchat.core.TouchPressType;
import edu.berkeley.wtchoi.swift.util.IdentifierPool;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/12/12
 * Time: 12:17 PM
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
public final class EnterCommand extends ICommand {
    private int x;
    private int y;
    private String content;
    private String toEnter;
    private int inputMethod;          //refer: http://developer.android.com/reference/android/widget/TextView.html#attr_android:inputMethod
    private boolean isInputMethodTarget;
    private boolean hasFocus = false;

    //All implementation of command should obtain integer identifier from
    private static final Integer tint = IdentifierPool.getFreshInteger();

    public EnterCommand(int x, int y, String content, String toEnter, int inputMethod, boolean focus, boolean isTarget){
        this.x = x;
        this.y = y;
        this.content = content;
        this.toEnter = toEnter;
        this.hasFocus = focus;
        this.inputMethod = inputMethod;
        this.isInputMethodTarget = isTarget;
    }

    @Override
    public void sendCommand(Driver driver) throws Device.CannotSendCommand, ApplicationTerminated, ApplicationCrash, InstrumentationCrash {
        super.sendCommandPrepare(driver);

        boolean cmdFlag = true;
        boolean  flag = true;
        if(!hasFocus){
            driver.device.touch(x,y,TouchPressType.DOWN_AND_UP);
            //driver.device.press(PhysicalButton.BACK, TouchPressType.DOWN_AND_UP);
            //super.sendCommandAck(driver);
            //flag = false;
        }

        for(char c: toEnter.toCharArray()){
            if(c == ' '){
                driver.device.press("KEYCODE_SPACE", TouchPressType.DOWN_AND_UP);
            }
            else{
                driver.device.type(String.valueOf(c));
            }
        }
        //if(flag){
        super.sendCommandAck(driver);
        //}
    }

    public Integer typeint(){
        return tint;
    }

    protected int compareSameType(ICommand target){
        EnterCommand cmd = (EnterCommand)target;
        int f = (new Integer(y)).compareTo(cmd.y);
        if(f == 0){
            f = (new Integer(x)).compareTo(cmd.x);
            if(f == 0){
                //return content.compareTo(cmd.content);
                boolean f1 = content.equals("");
                boolean f2 = cmd.content.equals("");
                if(f1 == f2) return 0;
                if(f1 && !f2) return 1;
                else return -1;
            }
        }
        return f;
    }

    protected boolean equalsToSameType(ICommand target){
        EnterCommand cmd = (EnterCommand) target;
        if(x == cmd.x && y == cmd.y){
            return content.equals("") == cmd.content.equals("");
        }
        return false;
    }

    public String toString(){
        String s1 = (!content.equals("")) ? "TE("+x+","+y+")" : "E("+x+","+y+")";
        String s2 =  getWidgetType() != null ? s1 + getWidgetType() : s1;
        String s3 = s2 + (hasFocus?"F":"");
        String s4 = s3 + (isInputMethodTarget()?"T":"");
        return s4;
    }

    public boolean isInputMethodTarget(){
        return isInputMethodTarget;
    }
}

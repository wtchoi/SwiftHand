package edu.berkeley.wtchoi.swift.driver;


import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;

import edu.berkeley.wtchoi.swift.util.IdentifierPool;
import edu.berkeley.wtchoi.swift.driver.Device.CannotSendCommand;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/26/12
 * Time: 8:50 PM
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
public final class PushCommand extends ICommand {

    public enum Type{
        MENU{ public String toString(){return "MENU";}},
        BACK{ public String toString(){return "BACK";}};
    }

    //All implementation of command should obtain integer identifier from
    private static final Integer typeint = IdentifierPool.getFreshInteger();

    private Type type;

    @Override
    protected void sendCommandImp(Driver driver) throws ApplicationTerminated, CannotSendCommand{
        switch(this.type){
            case MENU:
                //Code fragment for push MENU button
                driver.device.press(PhysicalButton.MENU, TouchPressType.DOWN_AND_UP);
                break;
            case BACK:
                driver.device.press(PhysicalButton.BACK, TouchPressType.DOWN_AND_UP);
                break;
        }
    }

    private PushCommand(Type t){
        type = t;
    }

    public static PushCommand getMenu(){
        return new PushCommand(Type.MENU);
    }

    public static PushCommand getBack(){
        return new PushCommand(Type.BACK);
    }
    
    protected int compareSameType(ICommand target){
        PushCommand cmd = (PushCommand) target;
        return type.compareTo(cmd.type);
    }

    protected boolean equalsToSameType(ICommand target){
        PushCommand cmd = (PushCommand) target;
        return type == cmd.type;
    }
    
    public Integer typeint(){
        return typeint;
    }

    public String toString(){
        return type.toString();
    }

    @Override
    public String getWidgetType(){
        return ".HardButton";
    }
}

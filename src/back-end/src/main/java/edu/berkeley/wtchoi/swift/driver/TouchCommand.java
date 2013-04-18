package edu.berkeley.wtchoi.swift.driver;

import com.android.chimpchat.core.TouchPressType;
import edu.berkeley.wtchoi.swift.util.IdentifierPool;
import edu.berkeley.wtchoi.swift.driver.Device.CannotSendCommand;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/26/12
 * Time: 1:27 PM
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
//Concrete ICommand Position. We are going to use this as an input character
public final class TouchCommand extends ICommand {//TODO
    protected Integer x;
    protected Integer y;

    //All implementation of command should obtain integer identifier from
    private static final Integer tint = IdentifierPool.getFreshInteger();

    protected int compareSameType(ICommand target){
        TouchCommand cmd = (TouchCommand) target;

        int c1 = y.compareTo(cmd.y);
        if (c1 == 0) {
            return x.compareTo(cmd.x);
        }
        return c1;
    }

    protected boolean equalsToSameType(ICommand target){
        TouchCommand cmd = (TouchCommand) target;
        return x == cmd.x && y == cmd.y;
    }

    public TouchCommand(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    @Override
    protected void sendCommandImp(Driver driver) throws ApplicationTerminated, CannotSendCommand{
        driver.device.touch(x, y, TouchPressType.DOWN_AND_UP);
    }

    /*
    @Override
    protected Integer getStableCountGoal(){
        return 1;
    }
    */

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer typeint(){
        return tint;
    }
    
    public String toString(){
        return ("(" + x + "," + y + "," + getWidgetType().substring(getWidgetType().lastIndexOf(".") + 1) + ")");
    }
}

package edu.berkeley.wtchoi.swift.driver;

import edu.berkeley.wtchoi.swift.util.IdentifierPool;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/16/12
 * Time: 3:10 PM
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
public final class LongTouchCommand extends ICommand{

    private Integer x, y;

    public LongTouchCommand(int x, int y){
        this.x = x;
        this.y = y;
    }

    private static final Integer tint = IdentifierPool.getFreshInteger();

    @Override
    public Integer typeint(){
        return tint;
    }

    @Override
    protected int compareSameType(ICommand target){
        LongTouchCommand cmd = (LongTouchCommand) target;

        int c1 = y.compareTo(cmd.y);
        if (c1 == 0) {
            return x.compareTo(cmd.x);
        }
        return c1;
    }

    @Override
    protected boolean equalsToSameType(ICommand target){
        LongTouchCommand cmd = (LongTouchCommand) target;
        return x == cmd.x && y == cmd.y;
    }

    @Override
    protected void sendCommandImp(Driver driver) throws ApplicationTerminated, Device.CannotSendCommand {
        driver.device.longTouch(x, y);
    }


    public String toString(){
        return ("L(" + x + "," + y + ")");
    }
}

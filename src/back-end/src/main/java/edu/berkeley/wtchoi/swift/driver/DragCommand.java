package edu.berkeley.wtchoi.swift.driver;

import edu.berkeley.wtchoi.swift.util.IdentifierPool;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/16/13
 * Time: 1:25 AM
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
public class DragCommand extends ICommand{
    protected Integer x_top;
    protected Integer y_top;
    protected Integer x_bot;
    protected Integer y_bot;
    protected boolean direction; //true = down, false = up

    //All implementation of command should obtain integer identifier from
    private static final Integer tint = IdentifierPool.getFreshInteger();

    protected int compareSameType(ICommand target){
        DragCommand cmd = (DragCommand) target;

        if(direction && !cmd.direction) return -1;
        if(!direction && cmd.direction) return 1;

        int c = x_top.compareTo(cmd.x_top);
        if (c == 0) {
            c = x_bot.compareTo(cmd.x_bot);
            if(c == 0){
                c = y_top.compareTo(cmd.y_top);
                if(c == 0)
                    return y_bot.compareTo(cmd.y_bot);
            }
        }
        return c;
    }

    protected boolean equalsToSameType(ICommand target){
        DragCommand cmd = (DragCommand) target;
        return direction == cmd.direction && x_top == cmd.x_top  &&  y_top == cmd.y_top  &&  x_bot == cmd.x_bot  &&  y_bot == cmd.y_bot;
    }

    private DragCommand(Integer x_top, Integer y_top, Integer x_bot, Integer y_bot, boolean direction) {
        this.x_top = x_top;
        this.y_top = y_top;
        this.x_bot = x_bot;
        this.y_bot = y_bot;
        this.direction = direction;
    }

    public static DragCommand getDownScorll(Integer x_top, Integer y_top, Integer x_bot, Integer y_bot){
        return new DragCommand(x_top, y_top, x_bot, y_bot, true);
    }

    public static DragCommand getUpScorll(Integer x_top, Integer y_top, Integer x_bot, Integer y_bot){
        return new DragCommand(x_top, y_top, x_bot, y_bot, false);
    }

    @Override
    protected void sendCommandImp(Driver driver) throws ApplicationTerminated, Device.CannotSendCommand {
        if(direction)
            driver.device.drag(x_top, y_top, x_bot, y_bot);
        else
            driver.device.drag(x_bot, y_bot, x_top, y_top);
    }

    public Integer typeint(){
        return tint;
    }

    public String toString(){
        if(direction)
            return ("DS((" + x_top + "," + y_top + "),(" + x_bot + "," + y_bot + ")," + getWidgetType().substring(getWidgetType().lastIndexOf(".") + 1) + ")");
        else
            return ("US((" + x_top + "," + y_top + "),(" + x_bot + "," + y_bot + ")," + getWidgetType().substring(getWidgetType().lastIndexOf(".") + 1) + ")");
    }

    public boolean isDualTo(DragCommand cmd){
        return x_top == cmd.x_top  &&  y_top == cmd.y_top  &&  x_bot == cmd.x_bot  &&  y_bot == cmd.y_bot && direction != cmd.direction;
    }

    @Override
    protected Integer getInitialSleep(Driver driver){
        if(driver.option.getTransitionTimeout() > 500) return null;
        return 500;
    }
}

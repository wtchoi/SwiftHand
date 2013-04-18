package edu.berkeley.wtchoi.swift.testing.android;

import edu.berkeley.wtchoi.swift.driver.EnterCommand;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.TouchCommand;
import edu.berkeley.wtchoi.swift.driver.ViewComponentInfo;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/1/13
 * Time: 6:29 PM
 * <p/>
 * SwiftHand Project follows BSD License
 * <p/>
 * [The "BSD license"]
 * Copyright (c) 2013 The Regents of the University of California.
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p/>
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
class TouchFactory implements ViewComponentInfo.PointFactory<ICommand> {
    public Collection<ICommand> get(int x, int y, ViewComponentInfo v) throws ViewToEvents.InputMethodActivated {
        Collection<ICommand> c = new LinkedList<ICommand>();

        if(v.isEditText() && (v.focusable() || v.hasOnClickListener())){
            String contents = (v.getInputMethod() & 0x2) == 0x2 ? "1" : "a";


            EnterCommand cmd = new EnterCommand(x, y, v.getTextContent(), contents, v.getInputMethod(), v.hasFocus(), v.isInputMethodTarget());
            cmd.setWidgetType(v.getViewType());
            c.add(cmd);
        }
        else{
            String vt = v.getViewType();
            if(vt.compareTo("android.widget.RelativeView") == 0){
                //ignore
            }
            else if(v.hasOnClickListener()){
                addTouch(c, x,y, v);
            }
            else{
                if(vt.compareTo("android.view.View") != 0
                        && vt.compareTo("android.widget.Button") != 0){
                    addTouch(c,x,y, v);
                }
            }
        }
        return c;
    }

    private void addTouch(Collection<ICommand> c, int x, int y, ViewComponentInfo v){
        TouchCommand command = new TouchCommand(x,y);
        command.setWidgetType(v.getViewType());
        c.add(command);
        //c.add(new LongTouchCommand(x, y));
    }
}

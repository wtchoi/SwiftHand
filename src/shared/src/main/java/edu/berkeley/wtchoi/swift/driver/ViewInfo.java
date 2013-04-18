package edu.berkeley.wtchoi.swift.driver;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/27/13
 * Time: 5:50 AM
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
public class ViewInfo implements Externalizable {

    private boolean isInputMethodActive = false;
    private boolean isInputMethodFullScreen = false;
    private ViewComponentInfo viewRoot;

    private static final long serialVersionUID = -2183309575573891457L;

    @Override
    public void writeExternal(ObjectOutput o) throws IOException {
        o.writeLong(serialVersionUID);
        writeTo(o);
    }

    private void writeTo(ObjectOutput o) throws IOException{
        o.writeBoolean(isInputMethodActive);
        o.writeBoolean(isInputMethodFullScreen);
        o.writeObject(viewRoot);
    }

    @Override
    public void readExternal(ObjectInput o) throws IOException, ClassNotFoundException{
        if(o.readLong() != serialVersionUID)
            throw new RuntimeException("Version miss match!");

        readFrom(o, this);
    }

    private static void readFrom(ObjectInput o, ViewInfo v) throws IOException, ClassNotFoundException{
        v.isInputMethodActive = o.readBoolean();
        v.isInputMethodFullScreen = o.readBoolean();
        v.viewRoot = (ViewComponentInfo) o.readObject();
    }

    public ViewInfo(){}

    public void setInputMethodActive(){
        isInputMethodActive = true;
    }

    public void setInputMethodFullScreen(){
        isInputMethodFullScreen = true;
    }

    public void setViewRoot(ViewComponentInfo vi){
        viewRoot = vi;
    }

    public boolean getInputMethodActive(){
        return isInputMethodActive;
    }

    public boolean getInputMethodFullScreen(){
        return isInputMethodFullScreen;
    }

    public ViewComponentInfo getViewRoot(){
        return viewRoot;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("ViewInfo: ");
        if(isInputMethodActive) builder.append("Active ");
        if(isInputMethodFullScreen) builder.append("FullScreen ");
        builder.append("\n");
        builder.append(viewRoot.toString());
        return builder.toString();
    }
}

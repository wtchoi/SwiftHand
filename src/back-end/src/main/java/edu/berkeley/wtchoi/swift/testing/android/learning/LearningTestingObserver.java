package edu.berkeley.wtchoi.swift.testing.android.learning;

import edu.berkeley.wtchoi.swift.testing.android.AppTestingObserver;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/30/13
 * Time: 1:00 AM
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
public class LearningTestingObserver extends AppTestingObserver {

    private AbstractLearningGuide guide;
    private boolean bigStepInteractiveMode = false;
    private boolean smallStepInteractiveMode = false;

    public LearningTestingObserver(String name){
        super(name);
    }

    public void setGuide(AbstractLearningGuide guide){
        this.guide = guide;
    }

    public void useBigStepInteractiveMode(){
        bigStepInteractiveMode = true;
    }

    public void useSmallStepInteractiveMode(){
        smallStepInteractiveMode = true;
    }

    protected void dumpOptional() throws IOException {
        guide.dumpInternal(path + "/" + testname);
    }

    protected void dumpIntermediate() throws IOException {
        guide.forceDumpInternal(path + "/intermediate");
    }

    protected void dumpException(Throwable e) throws IOException{
        File file = new File(path + "/" + testname + ".error");
        PrintWriter writer = new PrintWriter(file);
        e.printStackTrace(writer);
        writer.flush();
        writer.close();
    }

    @Override
    public void onTestItemBegin(){
        if(smallStepInteractiveMode || (bigStepInteractiveMode && guide.isWaitForNextSelection())){
            System.out.println("PRESS ENTER >> ");
            try{
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                br.readLine();
            }
            catch(Exception ignore){}
        }
        super.onTestItemBegin();
    }
}

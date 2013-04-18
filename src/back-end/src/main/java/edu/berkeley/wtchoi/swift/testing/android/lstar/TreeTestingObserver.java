package edu.berkeley.wtchoi.swift.testing.android.lstar;

import edu.berkeley.wtchoi.swift.testing.android.AppTestingObserver;
import edu.berkeley.wtchoi.logger.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/27/12
 * Time: 3:49 PM
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
public class TreeTestingObserver extends AppTestingObserver {
    public TreeTestingObserver(String name){
        super(name);
    }

    private TreeLearner learner;
    public void setLearner(TreeLearner leaner){
        this.learner = leaner;
    }

    @Override
    protected void dumpIntermediate(){
        learner.updateView();
        learner.dumpToDot(path + "/" + "lstar.dot");
    }

    @Override
    protected void dumpOptional(){
        learner.dumpToDot(path + "/" + testname + ".dot");
        try{
            FileWriter writer = new FileWriter(path + "/" + testname + ".seed");
            writer.write("Seed = " + learner.getSeed());
        }
        catch(IOException e){
            Logger.log("Cannot print seed");
        }
    }

    @Override
    public void dumpException(Throwable e) throws IOException {
        File file = new File(path + "/" + testname + ".error");
        PrintWriter writer = new PrintWriter(file);
        writer.println("seed = " + learner.getSeed());
        e.printStackTrace(writer);
        writer.flush();
        writer.close();
    }
}

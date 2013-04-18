package edu.berkeley.wtchoi.swift.testing.android.learning;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.collection.CPair;
import edu.berkeley.wtchoi.collection.CSet;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/12/13
 * Time: 1:54 PM
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
class LearningUtil{
    public static String stringOfPathPart(Collection<CPair<ICommand, CSet<ICommand>>> trace){
        StringBuilder builder = new StringBuilder();
        boolean flag = false;

        for(CPair<ICommand, CSet<ICommand>> pair:trace){
            if(flag) builder.append("::");
            else flag = true;
            builder.append(pair.getFirst().toString());
        }
        return builder.toString();
    }

    public static String stringOfPath(Collection<ICommand> path){
        StringBuilder builder = new StringBuilder();
        boolean flag = false;

        for(ICommand cmd : path){
            if(flag) builder.append("::");
            else flag = true;
            builder.append(cmd);
        }
        return builder.toString();
    }

    public static String stringOfPalette(CSet<ICommand> palette){
          return palette.toString();
    }

    public static String stringOfProgramPointSet(ProgramPointSet pps){
        final StringBuilder builder = new StringBuilder();

        pps.foreach(new ProgramPointSet.ProgramPointVisitor() {
            @Override
            public void visit(int bid, short mid) {
                try{
                    builder.append("(" + bid + "," + mid + ") ");
                }
                catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        });

        return builder.toString();
    }

    public static CSet<ICommand> getPalette(AppState state){
        if(state.isStop()) return new CSet<ICommand>();
        return state.getPalette();
        //return ViewToEvents.getRepresentativePoints(state.getViewInfo(), new CSet<ICommand>());
    }
}



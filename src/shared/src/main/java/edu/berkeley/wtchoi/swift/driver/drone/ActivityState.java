package edu.berkeley.wtchoi.swift.driver.drone;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/25/12
 * Time: 3:07 PM
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
public class ActivityState {
    private static enum State{
        Created("Created"),
        Start("Start"),
        Active("Active"),
        Pause("Pause"),
        Stop("Stop");



        final String str;

        private State(String s){
            this.str = s;
        }

        public String toString(){
            return str;
        }
    }
    // To track whether activity is enabled or not
    private State state;

    public ActivityState(){
        state = State.Created;
    }

    public void setActive(){
        state = State.Active;
    }

    public void setPause(){
        state = State.Pause;
    }

    public void setStop(){
        state = State.Stop;
    }

    public void setStart(){
        state = State.Start;
    }

    public boolean isStop(){
        return state == State.Stop;
    }

    public String toString(){
        return state.toString();
    }

    public boolean isPaused(){
        return state == State.Pause;
    }

    public boolean isActive(){
        return state == State.Active;
    }

    public boolean isStart(){
        return state == State.Start;
    }
}

package edu.berkeley.wtchoi.swift.testing.android.interactive;

import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.ManualCommand;
import edu.berkeley.wtchoi.swift.testing.android.AppRequest;
import edu.berkeley.wtchoi.swift.testing.android.AppResult;
import edu.berkeley.wtchoi.swift.testing.android.AppState;
import edu.berkeley.wtchoi.swift.testing.android.AppTestingGuide;
import edu.berkeley.wtchoi.collection.CVector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/19/13
 * Time: 2:00 PM
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
public class InteractiveGuide implements AppTestingGuide {
    public void setInitialState(AppState s){
        //TODO
        return;
    }

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public AppRequest getRequest(AppState s){
        System.out.println("====!!  SELECT  !!===");
        int index = 1;

        if(!s.isStop()){
            for (ICommand cmd :s.getPalette()){
                System.out.println(String.valueOf(index++) + ") " +  cmd);
            }
            System.out.println((index) + ") Manual");
        }
        System.out.println((s.isStop() ? 1 : (index+1)) + ") Reset");


        int selection;
        try{
            while(true){
                String k = "";
                while(k.length() == 0){
                    k = reader.readLine();
                }
                try{
                    selection = Integer.valueOf(k);
                    if(selection < 1 || selection > (index + 1)) continue;
                    break;
                }
                catch(NumberFormatException e){}
            }
        }
        catch(Exception e){ throw new RuntimeException(e) ;}


        CVector<ICommand> sequence = new CVector<ICommand>();
        AppRequest request = new AppRequest(sequence);
        if(selection == (s.isStop() ? 1 : index + 1)){
            request.setRestartRequest();
        }
        else if(selection == index){
            sequence.add(new ManualCommand());
        }
        else{
            int k = 1;
            for(ICommand cmd:s.getPalette()){
                if((k++) == selection){
                    sequence.add(cmd);
                    break;
                }
            }
        }
        return request;
    }

    public void receiveResult(AppRequest r, AppResult rr){
        //TODO
        return;
    }

    @Override
    public List<String> getOptionString(){
        return null;
    }

    @Override
    public void finish(){}
}

package edu.berkeley.wtchoi.swift.testing.android;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;
import edu.berkeley.wtchoi.swift.driver.drone.CompressedLog;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CVector;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/25/12
 * Time: 8:25 PM
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
public class AppResult{
    CList<ICommand> executedCommands = new CVector<ICommand>();
    LinkedList<AppState> resultingStateSequence = new LinkedList<AppState>();
    transient List<CompressedLog> resultingTraceSequence = new LinkedList<CompressedLog>();
    ProgramPointSet coverage = new ProgramPointSet();
    AppState startingState;
    boolean conflictFlag = false;
    boolean isStop = false;

    public void add(ICommand cmd, AppState appState, CompressedLog log){
        executedCommands.add(cmd);
        resultingStateSequence.add(appState);
        if(log != null){
            resultingTraceSequence.add(log);
        }
    }

    public void addStop(ICommand cmd, AppState state){
        executedCommands.add(cmd);
        resultingStateSequence.add(state);
        isStop = true;
    }

    public void updateCoverage(ProgramPointSet pps){
        coverage.addAll(pps);
    }

    public List<AppState> getResultingStateSequence(){
        return resultingStateSequence;
    }

    public List<CompressedLog> getResultingTraceSequence(){
        return resultingTraceSequence;
    }

    public CList<ICommand> getExecutedCommands(){
        return executedCommands;
    }

    public AppResult(AppState state){
        startingState = state;
    }

    public AppState getStaringState(){
        return startingState;
    }

    public void setCoverage(ProgramPointSet pps){
        coverage = pps;
    }

    public ProgramPointSet getCoverage(){
        return coverage;
    }

    public void setConflictFlag(boolean flag){
        conflictFlag = flag;
    }

    public boolean getConflictFlag(){
        return conflictFlag;
    }

    public boolean isStop(){
        return isStop;
    }

    private static Type cvectorType = new TypeToken<CVector<ICommand>>(){}.getType();
    private static Type listType = new TypeToken<LinkedList<AppState>>(){}.getType();
    public static class GsonAdapter implements JsonSerializer<AppResult>, JsonDeserializer<AppResult> {
        @Override
        public JsonElement serialize(AppResult src, Type typeOfSrc, JsonSerializationContext context){
            JsonObject result = new JsonObject();
            result.add("executedCommands", context.serialize(src.executedCommands, cvectorType));
            result.add("resultingStateSequence", context.serialize(src.resultingStateSequence, listType));
            //resultingTraceSequence
            result.add("coverage", context.serialize(src.coverage));
            result.add("startingState", context.serialize(src.startingState));
            result.addProperty("conflictFlag", src.conflictFlag);
            return result;
        }

        public AppResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
            JsonObject obj = json.getAsJsonObject();
            AppResult result = new AppResult((AppState) context.deserialize(obj.get("startingState"), AppState.class));
            result.executedCommands = context.deserialize(obj.get("executedCommands"), cvectorType);
            result.resultingStateSequence =  context.deserialize(obj.get("resultingStateSequence"), listType);
            //resultingTraceSequence
            result.coverage = context.deserialize(obj.get("coverage"), ProgramPointSet.class);
            result.startingState = context.deserialize(obj.get("startingState"), AppState.class);
            result.conflictFlag = obj.get("conflictFlag").getAsBoolean();

            return result;
        }
    }
}

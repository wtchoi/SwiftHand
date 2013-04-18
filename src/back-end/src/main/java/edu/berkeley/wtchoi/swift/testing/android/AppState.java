package edu.berkeley.wtchoi.swift.testing.android;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.swift.driver.ViewInfo;
import edu.berkeley.wtchoi.collection.CSet;
import edu.berkeley.wtchoi.collection.CVector;

import java.lang.reflect.Type;
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
public class AppState{
    private AppState(){}

    private static enum State{
        Live, Stop, Crash, Failed;
    }

    private CVector<ICommand> history;
    private transient ViewInfo view;
    private State state;
    private CSet<ICommand> palette;


    protected static AppState createLiveState(ViewInfo vi){
        AppState s = new AppState();
        s.state = State.Live;
        s.history = new CVector<ICommand>();
        s.view = vi;
        s.palette = ViewToEvents.getRepresentativePoints(vi, new CSet<ICommand>());
        return s;

    }

    public static AppState createSuccessorLiveState(AppState parent, ICommand cmd, ViewInfo vi){
        AppState s = new AppState();
        s.state = State.Live;

        s.history = new CVector<ICommand>(parent.history);
        s.history.add(cmd);

        s.view = vi;
        s.palette = ViewToEvents.getRepresentativePoints(vi, new CSet<ICommand>());
        return s;
    }

    public static AppState createSuccessorStopState(AppState parent, ICommand cmd){
        AppState s = new AppState();
        s.state = State.Stop;

        s.history = new CVector<ICommand>(parent.history);
        s.history.add(cmd);

        return s;
    }

    public ViewInfo getViewInfo(){
        return view;
    }

    public CSet<ICommand> getPalette(){
        return palette;
    }

    public boolean isStop(){
        return state == State.Stop;
    }

    public List<ICommand> getHistory(){
        return history;
    }

    public AppState copy(){
        AppState s = new AppState();
        s.state = this.state;
        s.history = new CVector(this.history);
        s.view = this.view;
        s.palette = this.palette;
        return s;
    }

    private static Type cvectorType = new TypeToken<CVector<ICommand>>(){}.getType();
    private static Type csetType = new TypeToken<CSet<ICommand>>(){}.getType();
    public static class GsonAdapter implements JsonSerializer<AppState>, JsonDeserializer<AppState> {
        @Override
        public JsonElement serialize(AppState src, Type typeOfSrc, JsonSerializationContext context){
            JsonObject result = new JsonObject();
            result.add("history", context.serialize(src.history, cvectorType));
            //view
            result.add("state", context.serialize(src.state));
            result.add("palette", context.serialize(src.palette, csetType));
            return result;
        }

        public AppState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
            JsonObject obj = json.getAsJsonObject();
            AppState state = new AppState();
            state.history = context.deserialize(obj.get("history"), cvectorType);
            state.palette = context.deserialize(obj.get("palette"), csetType);
            state.state = context.deserialize(obj.get("state"), State.class);
            //view

            return state;
        }
    }
}

package edu.berkeley.wtchoi.swift.testing.android;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.berkeley.wtchoi.swift.driver.ICommand;
import edu.berkeley.wtchoi.collection.CList;
import edu.berkeley.wtchoi.collection.CVector;

import java.lang.reflect.Type;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/25/12
 * Time: 5:38 PM
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

public class AppRequest{
    boolean requestRestart = false;
    boolean requestTrace = false;
    boolean requestReinstall = false;
    boolean requestRefreshView = false;

    CList<ICommand> inputSequence;
    transient Object extra;

    public AppRequest(CList<ICommand> inputSequence){
        this.inputSequence = inputSequence;
    }

    public void setRestartRequest(){
        requestRestart = true;
    }

    public void setRequestTrace(){
        requestTrace = true;
    }

    public void setReinstall(){
        requestReinstall = true;
    }

    public void setRequestRefreshView(){
        requestRefreshView = true;
    }

    public boolean requestRestart(){
        return requestRestart;
    }

    public CList<ICommand> getInputSequence(){
        return inputSequence;
    }

    public boolean requestTrace(){
        return requestTrace;
    }

    public void setExtra(Object object){
        extra = object;
    }

    public Object getExtra(){
        Object temp = extra;
        extra = null;
        return temp;
    }

    //Equivalence check, which ignore extra
    public boolean equalTo(AppRequest request){
        if(requestRestart != request.requestRestart) return false;
        if(requestTrace != request.requestTrace) return false;
        return inputSequence.equalsTo(request.inputSequence);
    }

    //Equivalence check, which consider extra
    public <T> boolean equalTo(AppRequest request, Comparator<T> comparator){
        if(!equalTo(request)) return false;
        T e1 = (T) extra;
        T e2 = (T) request.extra;

        return comparator.compare(e1, e2) == 0;
    }

    private static Type cvectorType = new TypeToken<CVector<ICommand>>(){}.getType();
    public static class GsonAdapter implements JsonSerializer<AppRequest>, JsonDeserializer<AppRequest> {
        @Override
        public JsonElement serialize(AppRequest src, Type typeOfSrc, JsonSerializationContext context){
            JsonObject result = new JsonObject();
            result.addProperty("requestRestart", src.requestRestart);
            result.addProperty("requestTrace", src.requestTrace);
            context.serialize(src.inputSequence, cvectorType);
            result.add("inputSequence", context.serialize(src.inputSequence, cvectorType));
            return result;
        }

        public AppRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
            JsonObject obj = json.getAsJsonObject();
            AppRequest request = new AppRequest((CVector<ICommand>) context.deserialize(obj.get("inputSequence"), cvectorType));
            request.requestRestart = obj.get("requestRestart").getAsBoolean();
            request.requestTrace = obj.get("requestTrace").getAsBoolean();

            return request;
        }
    }
}

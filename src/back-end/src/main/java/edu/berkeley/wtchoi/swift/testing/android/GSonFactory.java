package edu.berkeley.wtchoi.swift.testing.android;

import com.google.gson.*;
import edu.berkeley.wtchoi.swift.driver.ICommand;

import java.lang.reflect.Type;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/4/13
 * Time: 11:33 PM
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
class GSonFactory{

    private static Gson gson;

    public static Gson getInstance(){
        if(gson == null){
            gson = new GsonBuilder()
                    .registerTypeAdapter(AppRequest.class, new AppRequest.GsonAdapter())
                    .registerTypeAdapter(AppResult.class, new AppResult.GsonAdapter())
                    .registerTypeAdapter(AppState.class, new AppState.GsonAdapter())
                    .registerTypeAdapter(ICommand.class, new InterfaceAdapter<ICommand>())
                    .create();
        }
        return gson;
    }


    //GsonAdopter for abstract class ICommand
    //reference: http://ovaraksin.blogspot.com/2011/05/json-with-gson-and-abstract-classes.html
    public static class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context){
            JsonObject result = new JsonObject();
            result.add("type", new JsonPrimitive(src.getClass().getCanonicalName()));
            result.add("properties", context.serialize(src, src.getClass()));

            return result;
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            JsonElement element = jsonObject.get("properties");

            try{
                return context.deserialize(element, Class.forName(type));
            }
            catch(ClassNotFoundException cnfe){
                throw new JsonParseException("Unknown element type:" + type, cnfe);
            }
        }
    }
}

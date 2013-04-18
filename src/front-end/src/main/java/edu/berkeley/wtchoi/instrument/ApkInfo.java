package edu.berkeley.wtchoi.instrument;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

/**
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
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
public class ApkInfo{
    public int mInstructionCount;
    public int mMethodCount;
    public int mClassCount;
    public int mBranchCount;
    public String mAppMainPackage;
    public String mAppMainActivity;
    public String mApkPath;

    //unzipped list of pair of method id and method name
    public Integer[] methodIdentifiers;
    public String[] methodNames;

    //flattened list of decision pair
    public Integer[] decisionPoints;

    public static Gson gson = new Gson();

    public void loadInstrumentationResults(){
        try{
            Scanner mscanner = new Scanner(new java.io.File("/tmp/instrument.methods"));
            mscanner.useDelimiter("\n");

            String s1 = mscanner.next();
            methodIdentifiers = gson.fromJson(s1 , new TypeToken<Integer[]>(){}.getType());
            methodNames = gson.fromJson(mscanner.next() , new TypeToken<String[]>(){}.getType());

            FileReader creader = new FileReader("/tmp/instrument.decisionPoints");
            decisionPoints = gson.fromJson(creader, new TypeToken<Integer[]>(){}.getType());

            mBranchCount = decisionPoints.length / 2;
        }
        catch(FileNotFoundException e){
            throw new RuntimeException("Cannot file instrumentation side result files");
        }
    }

    public static void writeMethodIdentifierMap(Map<Integer, String> map){
        Integer[] ids = new Integer[map.size()];
        String[] names = new String[map.size()];

        int index = 0;
        for(Map.Entry<Integer, String> entry : map.entrySet()){
            ids[index] = entry.getKey();
            names[index] = entry.getValue();
            index++;
        }

        try{
            FileWriter writer = new FileWriter("/tmp/instrument.methods", false);
            gson.toJson(ids, new TypeToken<Integer[]>(){}.getType(), writer);
            writer.write("\n");
            gson.toJson(names, new TypeToken<String[]>(){}.getType(), writer);
            writer.close();
        }
        catch(IOException ie){
            throw new RuntimeException("Cannot create instrumentation side result file");
        }
    }

    public static void writeDecisionPoints(Integer[] flatarray){
        try{
            FileWriter writer = new FileWriter("/tmp/instrument.decisionPoints", false);
            gson.toJson(flatarray, new TypeToken<Integer[]>(){}.getType(), writer);
            writer.close();
        }
        catch(IOException e){
            throw new RuntimeException("Cannot create instrumentation side result file");
        }
    }
}

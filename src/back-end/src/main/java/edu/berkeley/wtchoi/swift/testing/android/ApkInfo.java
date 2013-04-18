package edu.berkeley.wtchoi.swift.testing.android;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.berkeley.wtchoi.swift.driver.ProgramPointSet;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 1/24/13
 * Time: 4:03 PM
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
public class ApkInfo {
    public int mInstructionCount;
    public int mMethodCount;
    public int mClassCount;
    public int mBranchCount;
    public String mAppMainPackage;
    public String mAppMainActivity;
    public String mApkPath;

    transient public Set<String> packageToExclude;
    transient public Set<Short> methodToExclude;

    //unzipped list of pair of method id and method name
    public Short[] methodIdentifiers;
    public String[] methodNames;

    //flattened list of decision pair
    public Integer[] decisionPoints;

    transient public ProgramPointSet decisionPointSet;
    transient public TreeMap<Short, String> methodMap;
    private transient int mFilteredBranchCount;

    public static Gson gson = new Gson();


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

    public static ApkInfo fromJson(String filename) throws FileNotFoundException{
        ApkInfo apkInfo = gson.fromJson(new FileReader(filename), ApkInfo.class);
        apkInfo.init();
        return apkInfo;
    }

    private void init(){
        decisionPointSet = new ProgramPointSet();
        int i =0;
        while(i < decisionPoints.length){
            int bid = decisionPoints[i++];
            short mid = (short) (int) decisionPoints[i++];
            decisionPointSet.add(bid, mid);
        }

        methodMap = new TreeMap<Short, String>();
        for(short j =0;j<methodNames.length;j++){
            methodMap.put(methodIdentifiers[j], methodNames[j]);
        }
    }

    public void setExclude(Set<String> packageToExclude){
        this.packageToExclude = packageToExclude;
        methodToExclude = new TreeSet<Short>();

        Outer:
        for(Map.Entry<Short,String> entry:methodMap.entrySet()){
            Inner:
            for(String pkg : packageToExclude){
                if(entry.getValue().startsWith(pkg)){
                    methodToExclude.add(entry.getKey());
                    break Inner;
                }
            }
        }

        mFilteredBranchCount = 0;
        decisionPointSet.foreach(new ProgramPointSet.ProgramPointVisitor(){
            public void visit(int bid, short mid){
                if(methodToExclude.contains(mid)) return;
                mFilteredBranchCount = mFilteredBranchCount + 1;
            }
        });
    }

    public boolean checkExclude(short mid){
        return methodToExclude != null && methodToExclude.contains(mid);
    }

    public int getBranchCount(){
        if(mFilteredBranchCount == 0) return mBranchCount;
        else return mFilteredBranchCount;
    }

    public void setPackageToExcludeFromFile(String filename){
        this.setExclude(readExcludingPackageList(filename));
    }

    private static Set<String> readExcludingPackageList(String filename){
        Set<String> set = new HashSet<String>();
        try{
            FileInputStream fis = new FileInputStream(filename);
            Scanner scanner = new Scanner(fis);
            scanner.useDelimiter("\n");

            while(scanner.hasNext()){
                String content = scanner.next();
                set.add(content);
                System.out.println(content);
            }

            return set;
        }
        catch(Exception ignore){
            ignore.printStackTrace();
        }
        return null;
    }
}

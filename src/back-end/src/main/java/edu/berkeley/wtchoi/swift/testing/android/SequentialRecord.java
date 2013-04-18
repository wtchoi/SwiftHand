package edu.berkeley.wtchoi.swift.testing.android;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 2/4/13
 * Time: 6:27 PM
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
public class SequentialRecord implements TargetApplication.TargetApplicationRecord{
    public static final String MAGIC = "SequentialRecord";

    private static Gson gson;
    private FileReader reader;
    private Scanner scanner;

    private AppRequest nextRequest = null;
    private AppResult nextResult = null;
    private boolean noRemainedRecord = false;

    public SequentialRecord(String inputFilename){
        try{
            gson = GSonFactory.getInstance();
            reader = new FileReader(inputFilename);
            scanner = new Scanner(reader);
            scanner.useDelimiter("\n");

            String content = scanner.next();

            if(content.compareTo(MAGIC) != 0)
                throw new RuntimeException("File doesn't have intended formant!");
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }

        readPair();
    }

    private void readPair(){
        if(noRemainedRecord) return;

        try{
            nextRequest = gson.fromJson(scanner.next(), AppRequest.class);
            nextResult = gson.fromJson(scanner.next(), AppResult.class);
        }
        catch(Exception e){
            noRemainedRecord = true;
        }

        int x = 1;
    }

    @Override
    public boolean hasNext(){
        return !noRemainedRecord;
    }

    @Override
    public boolean checkConsistency(AppRequest request){
        return request.equalTo(nextRequest);
    }

    @Override
    public AppResult getNext(){
        AppResult result = nextResult;
        readPair();
        return result;
    }
}



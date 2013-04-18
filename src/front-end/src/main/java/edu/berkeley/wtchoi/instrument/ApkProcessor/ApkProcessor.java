package edu.berkeley.wtchoi.instrument.ApkProcessor;

import edu.berkeley.wtchoi.instrument.DexPrinter;
import edu.berkeley.wtchoi.instrument.XmlPrinter;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/23/12
 * Time: 12:43 AM
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
public class ApkProcessor {


    public static abstract class FileTransformer{
        public abstract void transform(InputStream is, OutputStream os);
    }


    public void print(String inFileName) throws IOException {
        File inFile = new File(inFileName);
        if(!inFile.exists()) throw new RuntimeException("File not exists");

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(inFile));

        FileTransformer xmlTransformer = new XmlPrinter();
        FileTransformer dexTransformer = new DexPrinter();

        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while(zipEntry != null){
            String name = zipEntry.getName();
            if(xmlTransformer != null && name.compareTo("AndroidManifest.xml") == 0){
                File tf = createTempFile(zipInputStream, "AndroidManifest","xml");
                xmlTransformer.transform(new FileInputStream(tf), null);
            }
            else if(dexTransformer != null && name.compareTo("classes.dex") == 0){
                File tf = createTempFile(zipInputStream, "classes", "dex");
                dexTransformer.transform(new FileInputStream(tf), null);
            }
            else{
                zipInputStream.closeEntry();
            }
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry();
        }
    }

    public void instrument(String inFileName, String outFileName, FileTransformer xmlTransformer, FileTransformer dexTransformer) throws IOException{
        File inFile = new File(inFileName);
        File outFile = new File(outFileName);

        if(!inFile.exists()) throw new RuntimeException("File not exists");
        if(outFile.exists()){
            outFile.delete();
            outFile.createNewFile();
        }

        ApkProcessor apk = new ApkProcessor();

        File inputApkFile = inFile;
        File outputApkFile = outFile;


        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(inputApkFile));;
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputApkFile));;

        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while(zipEntry != null){
            String name = zipEntry.getName();
            if(xmlTransformer != null && name.compareTo("AndroidManifest.xml") == 0){
                File tf = createTempFile(zipInputStream, "AndroidManifest","xml");
                zipOutputStream.putNextEntry(new ZipEntry("AndroidManifest.xml"));
                xmlTransformer.transform(new FileInputStream(tf), zipOutputStream);
                zipOutputStream.closeEntry();
            }
            else if(dexTransformer != null && name.compareTo("classes.dex") == 0){
                File tf = createTempFile(zipInputStream, "classes", "dex");
                zipOutputStream.putNextEntry(new ZipEntry("classes.dex"));
                dexTransformer.transform(new FileInputStream(tf), zipOutputStream);
                zipOutputStream.closeEntry();
            }
            else if(name.startsWith("META-INF/")){
                zipInputStream.closeEntry();
            }
            else{
                zipOutputStream.putNextEntry(zipEntry);
                byte[] buf = new byte[1024];
                int len;
                while((len = zipInputStream.read(buf)) > 0){
                    zipOutputStream.write(buf, 0, len);
                }
                zipOutputStream.closeEntry();
            }
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry();
        }

        zipOutputStream.flush();
        zipOutputStream.close();
    }

    private File createTempFile(InputStream is, String prefix, String postfix) throws IOException{
        File tf = File.createTempFile(prefix,postfix);
        FileOutputStream tfos = new FileOutputStream(tf);
        byte[] buffer = new byte[1024];
        int len;
        while((len = is.read(buffer)) > 0){
            tfos.write(buffer, 0, len);
        }
        tfos.close();
        return tf;
    }
}


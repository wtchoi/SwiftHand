package edu.berkeley.wtchoi.swift.util;

import java.io.*;
import java.net.*;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/3/12
 * Time: 12:38 AM
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
public class DDNS {
    //Using API of oa.ta DDNS
    //URL scrapping code snippet from "http://www.mungchung.com/xe/index.php?mid=protip&category=3009&document_srl=4474"
    public static boolean regist(String addr, String id, String id2){
        try{
            URL url;
            StringBuffer url_content = new StringBuffer();
            String url_str = "http://oa.to/api/?type=update&id="+id+"&password="+id2 +"&host[" + addr + "]";
            System.out.println(url_str);
            url = new URL(url_str);

            InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            while(true){
                String inStr = br.readLine();
                if(inStr == null) break;
                url_content.append(inStr+"\r\n");
            }

            br.close();
            isr.close();
            is.close();

            System.out.println("URL updated");
            System.out.println(url_content.toString());
            return true;
        }
        catch(Exception e){
            return false;
        }
    }
}

package edu.berkeley.wtchoi.collection;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/26/12
 * Time: 7:57 PM
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

public class CollectionUtil {
    static public <T extends Comparable<T>> int compare(Collection<T> a, Collection<T> b) {
        int temp1 = a.size();
        int temp2 = b.size();

        if(temp1 > temp2) return 1;
        if(temp1 < temp2) return -1;

        Iterator<T> it1 = a.iterator();
        Iterator<T> it2 = b.iterator();

        //assumption : two collection have equivalent sie
        while (it1.hasNext()) {
            T t1 = it1.next();
            T t2 = it2.next();

            int temp = t1.compareTo(t2);
            if(temp != 0) return temp;
        }
        return 0;
    }

    static public <T> int compare(Collection<T> a, Collection<T> b, Comparator<T> comparator){
        int temp1 = a.size();
        int temp2 = b.size();

        if(temp1 > temp2) return 1;
        if(temp1 < temp2) return -1;

        Iterator<T> it1 = a.iterator();
        Iterator<T> it2 = b.iterator();

        while (it1.hasNext()) {
            T t1 = it1.next();
            T t2 = it2.next();

            int temp = comparator.compare(t1, t2);
            if(temp != 0) return temp;
        }
        return 0;
    }

    static public <T extends HasEqualityCheck<T>> boolean equal(Collection<T> a, Collection<T> b){
        int temp1 = a.size();
        int temp2 = b.size();

        if(temp1 != temp2) return false;

        Iterator<T> it1 = a.iterator();
        Iterator<T> it2 = b.iterator();

        //assumption : two collection have equivalent size
        while (it1.hasNext()) {
            T t1 = it1.next();
            T t2 = it2.next();

            if(!t1.equalsTo(t2)) return false;
        }
        return true;
    }


    static public <E,T> void writeTo(Collection<E> c, String opener, String separator, String closer, Writer writer)
        throws IOException {
        
        Iterator<E> iterator = c.iterator();

        if(!iterator.hasNext()){
            writer.write(opener);
            writer.write(closer);
            return;
        }

        writer.write(opener);
        do{
            E element = iterator.next();
            writer.write(element.toString());

            if(!iterator.hasNext()) break;
            writer.write(separator);
        }
        while(true);
        writer.write(closer);
    }
    
    static public <T> String stringOf(Collection<T> collection, String opener, String separator, String closer){
        StringWriter writer = new StringWriter();
        try {
            CollectionUtil.writeTo(collection, opener, separator, closer, writer);
            return writer.toString();
        }
        catch(IOException e){
            //Failed to generate string...
            return null;
        }
    }
}

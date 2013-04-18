package edu.berkeley.wtchoi.collection;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: cusgadmin
 * Date: 3/25/12
 * Time: 1:30 AM
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


//Comparable Set
public class CSet<T extends ExtendedComparable<T>> extends TreeSet<T> implements ExtendedComparable<CSet<T>> {
    public int compareTo(CSet<T> target) {
        return CollectionUtil.compare(this, target);
    }

    public boolean equalsTo(CSet<T> target){
        return CollectionUtil.equal(this, target);
    }

    public CSet(){}
    
    public CSet(Collection<T> collection) {
        super(collection);
    }
    
    public void writeTo(Writer writer) throws IOException{
        CollectionUtil.writeTo(this,"{",", ","}",writer);
    }
    
    public String toString(){
        return CollectionUtil.stringOf(this,"{",", ","}");
    }

    public T get(T element){
        return this.ceiling(element);
    }
}

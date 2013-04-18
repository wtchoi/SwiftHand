package edu.berkeley.wtchoi.collection;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 7:52 PM
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
//Comparable Vector
public class CVector<T extends ExtendedComparable<T>> extends Vector<T> implements CList<T>, Serializable {
    private static final long serialVersionUID = -5186309675577891457L;

    public int compareTo(CList<T> target) {
        return CollectionUtil.compare(this, target);
    }

    public boolean equalsTo(CList<T> target){
        return CollectionUtil.equal(this, target);
    }

    public CVector(int size) {
        super(size);
    }

    public CVector() {
        super();
    }

    public CVector(Iterable<T> collection) {
        super();
        for(T elt:collection){
            this.add(elt);
        }
    }

    public CVector(T[] array){
        super();
        for(T elt: array){
            this.add(elt);
        }
    }

    
    public void writeTo(Writer writer) throws IOException{
        CollectionUtil.writeTo(this,"[","; ","]",writer);
    }
    
    public String toString(){
        return CollectionUtil.stringOf(this,"[","; ","]") ;
    }
}

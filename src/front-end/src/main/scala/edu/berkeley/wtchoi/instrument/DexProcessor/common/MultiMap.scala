package edu.berkeley.wtchoi.instrument.DexProcessor.common

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 8/16/12
 * Time: 3:51 PM
 *
 * SwiftHand Project follows BSD License
 *
 * [The "BSD license"]
 * Copyright (c) 2013 Wontae Choi.
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

import collection.mutable.{Map,Set}
import com.sun.org.apache.xerces.internal.impl.dv.xs.YearDV
import collection.mutable

//Mutable MultiMap: X -> Pow(X)
class MultiMap[X,Y]{
  val map:Map[X,Set[Y]] = Map.empty[X,Set[Y]]

  def +=(elem:(X,Y)) : Unit = {
    var (key,v) = elem

    if (!map.contains(key))
      map += (key -> Set.empty[Y])

    map(key) += v
  }

  def -=(key:X) : Unit = {
    map.remove(key)
  }

  def apply(key:X) : Set[Y] = map(key)

  def contains(key:X) : Boolean = map.contains(key)

  def contains(key:X, value:Y) : Boolean = {
    if (map.contains(key)){
      return map(key).contains(value)
    }
    return false
  }

  def foreach(f:(X,Y) => Unit) : Unit = {
    var ff:((X,Set[Y])) => Unit = (elem) => (elem._2).foreach((value:Y) => f (elem._1,value))
    map.foreach(ff)
  }

  def foreachAsMap(f:((X,Set[Y])) => Unit):Unit = map.foreach(f)

  def size():Int = {
    var s = 0
    var f:((X,Set[Y])) => Unit =  (elem) => s += elem._2.size
    map.foreach(f)
    return s
  }

  override def toString():String = {
    var s = ""
    val f = (elem:(X,Set[Y])) => s = s + "\t" + elem._1.toString + "->" + elem._2.toString() + "\n"
    map.foreach(f)
    return "{\n" + s + "}"

  }
}

object MultiMap{
  def empty[X,Y]() = new MultiMap[X,Y]()
}

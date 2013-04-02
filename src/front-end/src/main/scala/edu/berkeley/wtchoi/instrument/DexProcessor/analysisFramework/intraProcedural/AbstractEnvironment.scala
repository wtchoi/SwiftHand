package edu.berkeley.wtchoi.instrument.DexProcessor.analysisFramework.intraProcedural

import java.lang.RuntimeException
import edu.berkeley.wtchoi.instrument.DexProcessor.il.BasicBlock

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/31/12
 * Time: 1:12 AM
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


class AbstractEnvironment[Key,V<:Domain[V]](seed:V, domain:Set[Key]) extends Domain[AbstractEnvironment[Key,V]]{
  private var map:Map[Key,V] = Map.empty
  private var polarity:Boolean = false

  def +=(kv:(Key,V)): AbstractEnvironment[Key,V] = { map += ((kv._1, kv._2)); this }

  def + (kv:(Key,V)): AbstractEnvironment[Key,V] = {
    val r = new AbstractEnvironment[Key,V](seed, domain)
    r.map = map + ((kv._1, kv._2))
    r.polarity = polarity
    return r
  }

  def clear():AbstractEnvironment[Key,V] = new AbstractEnvironment[Key,V](seed,domain)

  def addAll(map:Map[Key,V]): AbstractEnvironment[Key,V] = {
    var r = new AbstractEnvironment[Key,V](seed,domain)
    map.foreach((x) => {r += (x._1, x._2)})
    return r
  }

  def apply(k:Key):V = {
    if (!domain.contains(k))
      throw new RuntimeException("Key(" + k.toString + ") is not with in the domain abstract environment")

    if (map.contains(k)) return map(k)
    if (polarity) return (seed.top())
    return seed.bot()
  }

  override def bot(): AbstractEnvironment[Key,V] = new AbstractEnvironment[Key,V](seed, domain)

  override def top(): AbstractEnvironment[Key,V] ={
    val r = new AbstractEnvironment[Key,V](seed, domain)
    r.polarity = true
    return r
  }

  override def le(elt:AbstractEnvironment[Key,V]):Boolean = {
    return map.forall((kv) => (elt.map.contains(kv._1) && map(kv._1).le(elt.map(kv._1))))
  }

  override def join(elt:AbstractEnvironment[Key, V]): AbstractEnvironment[Key,V] = {
    val result = new AbstractEnvironment[Key,V](seed, domain)

    domain.foreach((key) => {
      result.map += key -> this(key).join(elt(key))
    })

    result.polarity = polarity || elt.polarity

    return result
  }

  def toMap():Map[Key, V] = {
    var m = map
    domain.foreach(key => {
      if (!map.contains(key)){
        if (!polarity) m += (key -> seed.bot())
        else m += (key -> seed.top())
      }
    })
    return m
  }

  private def folder(m:Map[Key,V], key:Key): Map[Key,V] = if (polarity) m + (key -> seed.top()) else m

  override def toString():String =
    (map ++ (domain -- map.keySet).foldLeft(Map.empty[Key,V])(folder)).toString()

}

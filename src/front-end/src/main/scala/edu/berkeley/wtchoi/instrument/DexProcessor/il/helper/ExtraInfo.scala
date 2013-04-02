package edu.berkeley.wtchoi.instrument.DexProcessor.il.helper

import collection.immutable.{IntMap, Queue}


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/2/12
 * Time: 10:07 AM
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
object ExtraInfo{
  private var schemas:Map[String, (Map[String, Int], IntMap[Class[Any]])] = Map.empty

  def create(schemaName:String): ExtraInfo = {
    if (!schemas.contains(schemaName)){
      throw new RuntimeException("Interpretation has to be defined before create ExtraInfo")
    }
    val (translation, types) = schemas(schemaName)
    return new ExtraInfo(translation, types){}
  }

  def defineInterpretation(schemaName:String, schema:Seq[(String, Class[Any])]){
    var translation = Map.empty[String, Int]
    var types = IntMap.empty[Class[Any]]

    var count = 0
    schema.foreach(x => {
      translation += x._1 -> count
      types += count -> x._2
      count = count + 1
    })

    schemas += schemaName -> (translation, types)
  }


}

abstract class ExtraInfo(translation:Map[String, Int], types:IntMap[Class[Any]]) {
  private var extras = IntMap.empty[Object]

  def putExtra(key:String, entry:Object):Unit = putExtra(translation(key), entry)
  def putExtra(index:Int, entry:Object):Unit = {
    extras += index -> entry
  }

  def getExtra[T](key:String):Option[T] ={
    if (translation.contains(key))return getExtra(translation(key))
    else None
  }

  def getExtra[T](index:Int):Option[T] = {
    if (extras.contains(index)){
      return Some(extras(index).asInstanceOf[T])
    }
    return None
  }

  def optExtra[T](key:String): T = optExtra[T](translation(key))
  def optExtra[T](index:Int):T = try{
    extras(index).asInstanceOf[T]
  }
  catch{
    case e:Exception => throw e
  }
}

package edu.berkeley.wtchoi.instrument.DexProcessor.classHierarchy

import edu.berkeley.wtchoi.instrument.DexProcessor.il.ClassInfo

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/12/12
 * Time: 4:12 PM
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
class ClassHierarchy {
  val objName = "Ljava.lang.Object;"

  implicit def ci2st(ci:ClassInfo):String = ci.name()

  private class Class(n:String){
    var superC:Class = null
    def setSuper(c:Class) = superC = c

    def name() = n
  }

  private var map = Map.empty[String, Class]
  private def get(s:String): Class = {
    if (!map.contains(s)) map += ( s -> new Class(s))
    return map(s)
  }

  def addDirectInheritance(className:String, superName:String){
    var classC = get(className)
    var superC = get(superName)
    classC.setSuper(superC)
  }

  def addRelation(iter:Iterable[(String, String)]){
    iter.foreach((x) => addDirectInheritance(x._1, x._2))
  }

  def checkInheritance(className:String, superName:String):Boolean = {
    var classC = get(className)
    var superC = get(superName)
    return checkInheritance(classC, superC)
  }

  private def checkInheritance(classC:Class, superC:Class): Boolean = {
    if (superC.name() == objName) return true

    if (classC.superC == null) return false
    if (classC.superC == superC) return true
    return checkInheritance(classC.superC, superC)
  }

  def getSuperClasses(cName:String): Set[String] = {
    val classC = get(cName)
    return getSuperClasses(classC)
  }

  private def getSuperClasses(classC:Class): Set[String] = {
    if (classC.superC == null) Set(objName)
    else Set(classC.superC.name) ++ getSuperClasses(classC.superC)
  }
}

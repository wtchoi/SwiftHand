package edu.berkeley.wtchoi.instrument.DexProcessor.il

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 9/13/12
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


class ClassInfo(_name:String, _appInfo:ApplicationInfo) {
  def name():String = _name

  private var superName:String = null
  private var interfaces:List[String] = null

  def setSuperName(s:String){
    superName = s
    _appInfo.addDirectInheritance(name, s)
  }

  def setInterfaces(ss:Array[String]) = if (ss != null) interfaces = ss.toList

  def getSuperName():String = superName

  private var methods = Map.empty[(String,String),MethodInfo]

  def registerMethod(access:Int, name:String, desc:String): MethodInfo = {
    val method = new MethodInfo(access, name, desc, this, _appInfo)
    val mid = _appInfo.registerMethod(method)
    method.setId(mid)

    methods += ((name,desc) -> method)

    return method
  }

  def getMethodInfo(name:String, desc:String):MethodInfo = methods((name,desc))

  def hasMethod(name:String, desc:String):Boolean = methods.contains(name, desc)

  def hasMethod(name:String) : Boolean = methods.exists(_._1._1 == name)

  def foreachMethod(f:(MethodInfo) => Unit) = methods.values.foreach(f)

  def includingApp():ApplicationInfo = _appInfo

  def isInherited():Boolean = (superName != null)

  def isInheritedFrom(superName:String) = _appInfo.checkInheritance(name, superName)

  def canOverrideMethod(methodName:String, desc:String):Boolean = !_appInfo.getSuperClasses(name).exists(ci =>{
    if (!ci.hasMethod(methodName,desc)) false
    else{
      val mi = ci.getMethodInfo(methodName,desc)
      mi.isFinal() && !mi.isPrivate()
    }
  })

  def isApplicationClass():Boolean = {
    (_appInfo.manifest().appClassDefined) && (_appInfo.manifest().getQualifiedAppClassName() == this.name())
  }

  def isMainActivityClass():Boolean = {_appInfo.manifest().getQualifiedMainActivityClassName == this.name()}
}

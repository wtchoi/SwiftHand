package edu.berkeley.wtchoi.instrument.DexProcessor.spec

import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.{ClassDescriptor, MethodDescriptor, InstrumentSpecification, CodeAssignment}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{ManifestInfo, ApplicationInfo, ClassInfo, MethodInfo}


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/16/12
 * Time: 10:54 AM
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
class SpecDrone() extends InstrumentSpecification{
  override def getCodeAssignment(mi:MethodInfo):CodeAssignment = {
    val spec:MethodSpecification = getSpec(mi)
    spec.getCodeAssignment(mi)
  }

  def getSpec(mi:MethodInfo):MethodSpecification = {
    val qn = mi.getQuantifiedName()
    val manifest:ManifestInfo = mi.includingApp().manifest()

    //Instrument bootstrap part1
    if (manifest.appClassDefined){
      if (mi.includingClass.isApplicationClass){
        mi.name() match{
          case "<init>" => return new MethodSpecApplicationConstructor
          case "<clinit>" => return new MethodSpecApplicationClassConstructor
          case "onCreate" => return new MethodSpecApplicationOnCreate
          case _ => ()
        }
      }
    }

    var target = List("onCreate", "onStart", "onResume", "onPause", "onStop", "onDestroy")
    if (mi.includingClass().isInheritedFrom("Landroid/app/Activity;")){
      mi.name() match{
        case event if C.eventNameCheck(event,target) => return new MethodSpecActivityOn(event)
        case _ => ()
      }
    }

    if (mi.isStaticMethod()) return new MethodSpecStatic

    if (mi.isConstructor() && mi.includingClass().isInherited())
      return new MethodSpecConstructorInherited

    return new MethodSpecVirtual
  }


  override def getAdditionalClasses(appInfo:ApplicationInfo): Iterable[ClassDescriptor] = {
    var classDesc : List[ClassDescriptor] = List.empty

    if (!appInfo.manifest().appClassDefined){
      classDesc = (new ClassDescMockApplication)::classDesc
    }

    return classDesc
  }


  override def getAdditionalMethods(classInfo:ClassInfo):Iterable[MethodDescriptor] = {
    var methods = List.empty[MethodDescriptor]

    val checkNecessity : (String,String) => Boolean
    = (methodName, methodDesc) => (
      (!classInfo.hasMethod(methodName))
        && classInfo.canOverrideMethod(methodName, methodDesc)
      )

    if (classInfo.isApplicationClass() && !classInfo.hasMethod("onCreate", "V")){
      methods = (new MethodDescApplicationOnCreate)::methods
    }
    else if (classInfo.isInheritedFrom("Landroid/app/Activity;")){
      if (checkNecessity("onCreate", "VLandroid/os/Bundle;")){
        methods = (new MethodDescOnCreate)::methods
      }

      val targets = List("onStart", "onResume","onPause", "onStop", "onDestroy")
      targets.foreach(event => {
        if (checkNecessity(event, "V"))
          methods = (new MethodDescOn(event))::methods
      })
    }
    return methods
  }
}

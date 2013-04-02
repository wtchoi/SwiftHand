package edu.berkeley.wtchoi.instrument.DexProcessor.il

import helper.ExtraInfo
import edu.berkeley.wtchoi.instrument.DexProcessor.classHierarchy.ClassHierarchy

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 9/13/12
 * Time: 4:11 PM
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


class ApplicationInfo(manifestInfo:ManifestInfo){
  {
    val keys:Seq[(String, Class[Any])] = Seq(
      (IL.BB.LiveRegisterIn, IL.LiveSetClass),
      (IL.BB.LiveRegisterOut,IL.LiveSetClass),
      (IL.BB.RegisterTypeIn, IL.RegisterTypesClass),
      (IL.BB.RegisterTypeOut, IL.RegisterTypesClass))

    ExtraInfo.defineInterpretation(IL.BB.name, keys)
  }

  private var classes = Map.empty[String,ClassInfo]
  private var classHierarchy:ClassHierarchy = new ClassHierarchy
  private var methodCount = 0
  private var instructionCount = 0

  def registerClass(name:String): ClassInfo = {
    val classInfo = new ClassInfo(name, this)
    classes += (name -> classInfo)
    return classInfo
  }

  def getClassInfo(name:String):ClassInfo = classes(name)

  def foreachClass(f:(ClassInfo) => Unit){
    classes.foreach((elt:(String,ClassInfo)) => f(elt._2))
  }

  def foreachMethod(f:(MethodInfo) => Unit){
    val cf = (c:ClassInfo) => c.foreachMethod(f)
    foreachClass(cf)
  }

  def exportToDot(path:String) = foreachMethod((m:MethodInfo) => m.exportToDot(path))

  def registerMethod(methodInfo:MethodInfo):Int = {
    methodCount = methodCount + 1
    return methodCount
  }

  {
    classHierarchy.addRelation(List(
      //Activity Hierarchy
      ("Landroid/accounts/AccountAuthenticatorActivity;", "Landroid/app/Activity;"),
      ("Landroid/app/ActivityGroup;", "Landroid/app/Activity;"),
      ("Landroid/app/AliasActivity;", "Landroid/app/Activity;"),
      ("Landroid/support/v13/dreams/BasicDream;", "Landroid/app/Activity;"),
      ("Landroid/app/ExpandableListActivity;", "Landroid/app/Activity;"),
      ("Landroid/support/v4/app/FragmentActivity;", "Landroid/app/Activity;"),
      ("Landroid/app/ListActivity;", "Landroid/app/Activity;"),
      ("Landroid/app/NativeActivity;","Landroid/app/Activity;"),

      ("Landroid/app/LauncherActivity;", "Landroid/app/ListActivity;"),
      ("Landroid/preference/PreferenceActivity;", "Landroid/app/ListActivity;"),
      ("Landroid/app/TabActivity;", "Landroid/app/ActivityGroup;"),

      //Content Provider Hierarchy
      ("Landroid/content/SearchRecentSuggestionsProvider;", "Landroid/content/ContentProvider;")
    ))
  }
  def addDirectInheritance(cName:String, sName:String) =
    classHierarchy.addDirectInheritance(cName, sName)

  def checkInheritance(cName:String, sName:String):Boolean =
    classHierarchy.checkInheritance(cName, sName)

  def getSuperClasses(cName:String):Set[ClassInfo] = {
    val superNames = classHierarchy.getSuperClasses(cName).filter(classes.contains(_))
    superNames.map(classes(_))
  }

  def manifest() = manifestInfo

  def increaseInstructionCount() = instructionCount += 1
  def getInstructionCount() = instructionCount
  def getClassCount() = classes.size
  def getMethodCount() = methodCount
}

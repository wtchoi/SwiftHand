package edu.berkeley.wtchoi.instrument.DexProcessor.ilbuilder.imp

import org.ow2.asmdex.{MethodVisitor, ClassVisitor}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.ClassInfo
import edu.berkeley.wtchoi.instrument.util.Debug
/*
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

/*
Class Visitor for ApplicationInfo Building
-----------------------------
The main obligation of this class is:
  A. setting current context information (method name)
  B. providing ILBuilderMethodVisitor class
 */


class ILBuilderClassVisitor(api: Int, classInfo:ClassInfo) extends ClassVisitor(api, null) {

  override def visitMethod(access: Int, name: String, desc: String, sig: Array[String], exception: Array[String]): MethodVisitor = {

    Debug.println(classInfo.name() + "::" + name)

    val methodInfo = classInfo.registerMethod(access, name, desc)
    return new ILBuilderMethodVisitor(api, access, methodInfo)
  }
}

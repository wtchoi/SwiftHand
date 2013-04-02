package edu.berkeley.wtchoi.instrument.DexProcessor.instrument

import org.ow2.asmdex.{MethodVisitor, ClassVisitor}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{ClassInfo}


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 9/20/12
 * Time: 10:45 PM
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
class InstrumentingClassVisitor(classInfo:ClassInfo, api:Int, cv:ClassVisitor, spec:InstrumentSpecification) extends ClassVisitor(api,cv){
  override def visitMethod(access: Int, name: String, desc: String, sig: Array[String], exception: Array[String]): MethodVisitor = {
    val mv:MethodVisitor = cv.visitMethod(access,name,desc,sig,exception)
    val methodInfo = classInfo.getMethodInfo(name,desc)

    if (methodInfo.isAbstractMethod() || methodInfo.isNative())
      return mv;

    return new InstrumentingMethodVisitor(methodInfo, api, mv, spec.getCodeAssignment(methodInfo))
  }

  val supervisor:String = "Ledu/berkeley/wtchoi/cc/driver/drone/Supervisor;"

  override def visitEnd(){
    val methods = spec.getAdditionalMethods(classInfo)
    methods.foreach(_.exportTo(classInfo, cv))
    cv.visitEnd()
  }
}

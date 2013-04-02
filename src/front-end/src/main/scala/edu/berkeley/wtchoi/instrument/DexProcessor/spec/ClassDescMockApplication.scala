package edu.berkeley.wtchoi.instrument.DexProcessor.spec

import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.ClassDescriptor
import edu.berkeley.wtchoi.instrument.DexProcessor.il.{ApplicationInfo}
import org.ow2.asmdex.{Opcodes, ApplicationVisitor}
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/9/12
 * Time: 11:26 PM
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
class ClassDescMockApplication extends ClassDescriptor{
  override def exportTo(appInfo:ApplicationInfo, av:ApplicationVisitor){
    var opt:Int = Opcodes.ACC_PUBLIC

    val ci = appInfo.registerClass(appInfo.manifest().getQualifiedAppClassName())
    ci.setSuperName("Landroid/app/Application;")

    val cv = av.visitClass(opt, ci.name(), null, ci.getSuperName(), null)
    cv.visit(0, opt, ci.name(), null, ci.getSuperName(), null)



    {
      val access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
      val mi = ci.registerMethod(access, "<clinit>", "V")
      val mid:Int = appInfo.registerMethod(mi)

      val mv = cv.visitMethod(access, "<clinit>", "V", null, null)
      mv.visitParameters(Array.empty)
      mv.visitCode()
      mv.visitMaxs(1,0)
      mv.visitInsn(Opcode.RETURN_VOID.encode())
      mv.visitEnd()
    }



    {
      val access = Opcodes.ACC_PUBLIC | Opcodes.ACC_CONSTRUCTOR
      val mi = ci.registerMethod(access, "<init>","V")
      val mid:Int = appInfo.registerMethod(mi)

      val mv = cv.visitMethod(access, "<init>", "V", null, null)
      val invoker = new SupervisorInvoker(mv);

      mv.visitParameters(Array.empty)
      mv.visitCode()
      mv.visitMaxs(3,0)

      mv.visitVarInsn(Opcode.CONST.encode(),0,mid)
      mv.visitVarInsn(Opcode.CONST.encode(),1,0)

      invoker.appPrepare()
      invoker.logEnter(0)
      invoker.logProgramPoint(1,0)
      invoker.logCall(0)

      mv.visitMethodInsn(Opcode.INVOKE_DIRECT.encode(), ci.getSuperName(), "<init>", "V", Array(2))

      invoker.appRegisterApplication(2)
      invoker.logReturn(0)
      invoker.logReceiver(2,0)
      invoker.logExit(0)

      mv.visitInsn(Opcode.RETURN_VOID.encode())
      mv.visitEnd()
    }



    {
      val access = Opcodes.ACC_PUBLIC
      val mi = ci.registerMethod(access, "onCreate","V")
      val mid:Int = appInfo.registerMethod(mi)

      val mv = cv.visitMethod(access, "onCreate", "V", null, null)
      val invoker = new SupervisorInvoker(mv)

      mv.visitParameters(Array.empty)
      mv.visitCode()
      mv.visitMaxs(3,0)

      mv.visitVarInsn(Opcode.CONST.encode(),0,mid)
      mv.visitVarInsn(Opcode.CONST.encode(),1,0)

      invoker.appStart()
      invoker.logEnter(0)
      invoker.logReceiver(2,0)
      invoker.logProgramPoint(1,0)
      invoker.logCall(0)

      mv.visitMethodInsn(Opcode.INVOKE_SUPER.encode(), ci.getSuperName(), "onCreate", "V", Array(2))

      invoker.logReturn(0)
      invoker.logExit(0)

      mv.visitInsn(Opcode.RETURN_VOID.encode())
      mv.visitEnd()
    }

    cv.visitEnd()
  }
}

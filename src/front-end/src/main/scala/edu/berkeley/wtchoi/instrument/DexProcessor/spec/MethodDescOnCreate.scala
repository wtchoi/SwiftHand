package edu.berkeley.wtchoi.instrument.DexProcessor.spec

import org.ow2.asmdex.{MethodVisitor, ClassVisitor, Opcodes}
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import edu.berkeley.wtchoi.instrument.DexProcessor.il.ClassInfo
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.MethodDescriptor


/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/16/12
 * Time: 12:17 PM
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
class MethodDescOnCreate extends MethodDescriptor{
  def exportTo(classInfo:ClassInfo, cv:ClassVisitor){

    val access = Opcodes.ACC_PROTECTED
    val mi = classInfo.registerMethod(access, "onCreate","VLandroid/os/Bundle;")
    val mid:Int = classInfo.includingApp().registerMethod(mi)

    val mv:MethodVisitor = cv.visitMethod(access, "onCreate", "VLandroid/os/Bundle;", null, null)
    val invoker = new SupervisorInvoker(mv)

    mv.visitParameters(Array("savedInstance"))
    mv.visitCode()
    mv.visitMaxs(4,0)

    mv.visitVarInsn(Opcode.CONST.encode(),0,mid)
    mv.visitVarInsn(Opcode.CONST.encode(),1,0)

    invoker.logActivityCreatedEnter(2)
    invoker.logEnter(0)
    invoker.logReceiver(2,0)
    invoker.logProgramPoint(1,0)
    invoker.logCall(0)

    mv.visitMethodInsn(Opcode.INVOKE_SUPER.encode(), classInfo.getSuperName, "onCreate", "VLandroid/os/Bundle;", Array(2,3))

    invoker.logReturn(0)
    invoker.logExit(0)
    invoker.logActivityCreatedExit(2)

    mv.visitInsn(Opcode.RETURN_VOID.encode())

    mv.visitEnd()
  }
}

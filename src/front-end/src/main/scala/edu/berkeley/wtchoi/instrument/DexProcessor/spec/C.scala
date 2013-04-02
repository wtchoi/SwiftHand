package edu.berkeley.wtchoi.instrument.DexProcessor.spec

import scala.Predef._
import scala.Array
import edu.berkeley.wtchoi.instrument.DexProcessor.Opcode
import org.ow2.asmdex.MethodVisitor

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
package object C{
  val supervisor = "Ledu/berkeley/wtchoi/swift/driver/drone/Supervisor;"
  val classApplication = "Landroid/app/Application;"

  def eventNameCheck(event:String, spec:List[String]):Boolean = spec.contains(event)
}

class SupervisorInvoker(mv:MethodVisitor){
  private val invoke : (String, String, Array[Int]) => Unit
  = (fName, desc, args) => mv.visitMethodInsn(Opcode.INVOKE_STATIC.encode(), C.supervisor, fName, desc, args)

  def appPrepare() = invoke("appPrepare", "V", Array.empty)

  def appRegisterApplication(r:Int) = invoke("appRegisterApplication", "VLandroid/app/Application;", Array(r))

  def appStart() = invoke("appStart", "V", Array.empty)

  def logEnter(r:Int) = invoke("logEnter", "VS", Array(r))

  def logProgramPoint(r1:Int, r2:Int) = invoke("logProgramPoint", "VIS", Array(r1,r2))

  def logCall(r:Int) = invoke("logCall", "VS", Array(r))

  def logReturn(r:Int) = invoke("logReturn", "VS", Array(r))

  def logReceiver(r1:Int, r2:Int) = invoke("logReceiver", "VLjava/lang/Object;S", Array(r1,r2))

  def logExit(r:Int) = invoke("logExit", "VS", Array(r))

  def logActivityCreatedEnter(r:Int) = invoke("logActivityCreatedEnter", "VLandroid/app/Activity;", Array(r))
  def logActivityCreatedExit(r:Int) = invoke("logActivityCreatedExit", "VLandroid/app/Activity;", Array(r))

  def logResumeEnter(r:Int) = invoke("logResumeEnter", "VLandroid/app/Activity;", Array(r))
  def logResumeExit(r:Int) = invoke("logResumeExit", "VLandroid/app/Activity;", Array(r))

  def logStartEnter(r:Int) = invoke("logStartEnter", "VLandroid/app/Activity;", Array(r))
  def logStartExit(r:Int) = invoke("logStartExit", "VLandroid/app/Activity;", Array(r))

  def logStopEnter(r:Int) = invoke("logStopEnter", "VLandroid/app/Activity;", Array(r))
  def logStopExit(r:Int) = invoke("logStopExit", "VLandroid/app/Activity;", Array(r))

  def logPauseEnter(r:Int) = invoke("logPauseEnter", "VLandroid/app/Activity;", Array(r))
  def logPauseExit(r:Int) = invoke("logPauseExit", "VLandroid/app/Activity;", Array(r))

  def logDestroyEnter(r:Int) = invoke("logDestroyEnter", "VLandroid/app/Activity;", Array(r))
  def logDestroyExit(r:Int) = invoke("logDestroyExit", "VLandroid/app/Activity;", Array(r))
}

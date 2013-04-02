package edu.berkeley.wtchoi.instrument.DexProcessor.spec

import edu.berkeley.wtchoi.instrument.DexProcessor.instrument._
import collection.immutable.Queue

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/12/12
 * Time: 2:34 PM
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
trait DSL{
  type CodeBlock = Instrumentation.CodeBlock

  protected def code():Queue[Command]
  protected def addToCode(c:Command)

  def getCode():CodeBlock = code

  class Variable(x:String){
    def :=(exp:Expression) =  addToCode(CmdAssign(x,exp))
  }

  implicit def symbol2String(x:Symbol) = x.toString()
  implicit def symbol2Variable(x:Symbol) = new Variable(x.toString())
  implicit def symbol2Ty(ty:Symbol) = ty match {
    case 'I => TyInt
    case 'Z => TyBoolean
    case 'S => TyShort
    case 'V => TyVoid
    case 'Str => TyString
    case 'Object => TyObject
    case 'Exception => TyException
    case _ => TyClass(ty.toString())
  }

  implicit def symbol2Exp(exp:Symbol):Expression = {
    exp match {
      case 'methodName => ExpMethodName
      case 'currentOffset => ExpOffset
      case 'this => ExpThis()
      case 'thisObject => ExpThis() as TyObject
      case x => ExpVar(x)
    }
  }
  implicit def int2Exp(n:Int) = ExpInt(n)
  implicit def str2Exp(s:String) = ExpStr(s)
  implicit def str2Ty(s:String) = TyClass(s)

  def sget(cname:String, fname:String, ty:Ty)
  = ExpStaticField(cname,fname,ty)

  def variable(x:String, ty:Ty)
  = addToCode(CmdDeclareVariable(x, ty))

  def invokeS(cName:String, mName:String, args:Queue[Expression], returnTy:Ty){
    val arguments = if (args == null) Queue.empty else args
    addToCode(CmdInvokeStatic(cName, mName, arguments, returnTy))
  }

  def invokeSV(cName:String, mName:String, args:Queue[Expression]){
    val arguments = if (args == null) Queue.empty else args
    addToCode(CmdInvokeStatic(cName, mName, arguments, TyVoid))
  }

  def invokeV(cName:String, mName:String, args:Queue[Expression], returnTy:Ty){
    val arguments = if (args == null) Queue.empty else args
    addToCode(CmdInvokeVirtual(cName, mName, arguments, returnTy))
  }

  def throwException(x:String) = addToCode(CmdThrowException(x))

  def loadResultTo(x:String) = addToCode(CmdLoadResult(x))

  def loadExceptionTo(x:String) = addToCode(CmdLoadException(x))

  def nop() = addToCode(CmdNop)
}

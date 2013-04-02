package edu.berkeley.wtchoi.instrument.DexProcessor.instrument


import collection.immutable.Queue
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


sealed abstract class Ty{
  def descriptor():String
  def isObjectTy:Boolean
}
case object TyInt extends Ty{
  def descriptor():String = "I"
  def isObjectTy:Boolean = false
}

case object TyShort extends Ty{
  def descriptor():String = "S"
  def isObjectTy:Boolean = false
}

case object TyBoolean extends Ty{
  def descriptor():String = "Z"
  def isObjectTy:Boolean = false
}

case object TyString extends Ty{
  def descriptor():String = "Ljava/lang/String;"
  def isObjectTy:Boolean = true
}

case object TyObject extends Ty{
  def descriptor():String = "Ljava/lang/Object;"
  def isObjectTy:Boolean = true
}

case object TyVoid extends Ty{
  def descriptor():String = "V"
  def isObjectTy:Boolean = false
}

case class TyClass(desc:String) extends Ty{
  def descriptor():String = desc
  def isObjectTy:Boolean = true
}

case object TyException extends TyClass("Ljava/lang/Exception;")



sealed abstract class Command
case object CmdNop extends Command
case class CmdDeclareVariable(varName:String, typ:Ty) extends Command
case class CmdAssign(varName:String, exp:Expression) extends Command
case class CmdInvokeStatic  (cName: String, mName: String, args: Queue[Expression], returnTy:Ty) extends Command
case class CmdInvokeVirtual (cName: String, mName: String, args: Queue[Expression], returnTy:Ty) extends Command
case class CmdLoadResult (varName:String) extends Command
case class CmdLoadException (varName:String) extends Command
case class CmdThrowException (varName:String) extends Command


object Ty{
  def assertEqual(a:Ty, b:Ty){
    val da = a.descriptor()
    val db = b.descriptor()
    if( da == db ) return
    Debug.WTF("Type Mismatch!");
  }
}

sealed abstract class Expression{
  def ty(tyEnv:Map[String,Ty]):Ty
}
case class ExpThis() extends Expression{
  //assume type environment always contains types for receiver class
  private var typ:Ty = null

  override def ty(tyEnv:Map[String,Ty]):Ty = if(typ == null) tyEnv("this") else typ
  def as(_ty:Ty) = {
    typ = _ty
    this
  }
}

case object ExpOffset extends Expression{
  override def ty(tyEnv:Map[String,Ty]):Ty = TyInt
}

case object ExpMethodName extends Expression{
  override def ty(tyEnv:Map[String,Ty]):Ty = TyString
}

case class ExpInt(i: Int) extends Expression{
  override def ty(tyEnv:Map[String,Ty]):Ty = TyInt
}

case class ExpShort(i:Short) extends Expression{
  def this(i:Int) = this(i.toShort)
  override def ty(tyEnv:Map[String,Ty]):Ty = TyShort
}

case class ExpStr(s: String) extends Expression{
  override def ty(tyEnv:Map[String,Ty]):Ty = TyString
}

case class ExpVar(x: String) extends Expression{
  override def ty(tyEnv:Map[String,Ty]):Ty = tyEnv(x)
}

case class ExpStaticField(cName:String, field:String, _ty:Ty) extends Expression{
  override def ty(tyEnv:Map[String,Ty]):Ty = _ty
}
//case class ExpObjectField(cName:String, field:String, ty:Ty, objectVar:String) extends Expression


object Instrumentation{
  type CodeBlock = Queue[Command]
}


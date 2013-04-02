package edu.berkeley.wtchoi.instrument.DexProcessor.il

import edu.berkeley.wtchoi.instrument.util.Debug
import java.io.{BufferedWriter, FileWriter, FileOutputStream, File}
import collection.immutable.Queue
import types.{TypeVoid, Type}
import edu.berkeley.wtchoi.instrument.DexProcessor.il.types.Type
import org.ow2.asmdex.Opcodes
import edu.berkeley.wtchoi.gv.GraphViz

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
class MethodInfo(_access:Int, _name:String, _desc:String, _class:ClassInfo, _app:ApplicationInfo) {
  def name():String = _name

  private var __id:Int = -1
  def setId(_id:Int) = __id = _id
  def id = __id

  private var app = _app

  private def checkAccess(flag:Int) = (_access & flag) == flag

  def isAbstractMethod():Boolean = checkAccess(Opcodes.ACC_ABSTRACT)
  def isStaticMethod():Boolean = checkAccess(Opcodes.ACC_STATIC)
  def isConstructor():Boolean = checkAccess(Opcodes.ACC_CONSTRUCTOR)
  def isFinal():Boolean = checkAccess(Opcodes.ACC_FINAL)
  def isPublic():Boolean = checkAccess(Opcodes.ACC_PUBLIC)
  def isProtected():Boolean = checkAccess(Opcodes.ACC_PROTECTED)
  def isPrivate():Boolean = checkAccess(Opcodes.ACC_PRIVATE)
  def isNative():Boolean = checkAccess(Opcodes.ACC_NATIVE)

  private var actualParameterTypes:Queue[Type] = null
  private var returnType:Type = null
  typeInit()

  private def typeInit() {
    val typeSequence = Type.typeSequenceOf(_desc)
    if(typeSequence.tail.exists(_ == TypeVoid))
      Debug.WTF("Parameter cannot have void type!")
    actualParameterTypes = typeSequence.tail
    returnType = typeSequence.head
  }

  //The number of parameter registers. This does not include thisRegister
  private var actualParameterCount:Int = {
    var size = 0
    actualParameterTypes.foreach(size += _.size)
    size
  }

  def getParameterCount():Int = {
    if (isNative()) return 0
    if (isAbstractMethod()) return 0 //TODO: check: Is there code depend on this line?
    if (isStaticMethod) return actualParameterCount
    //if (isConstructor) return actualParameterCount + 1
    return actualParameterCount + 1
  }

  def getActualParameterCount():Int = actualParameterCount

  def getActualParametersInfo() : Seq[(Int, Type)] = {
    getFirstActualParameterRegister() match{
      case None =>
        return Seq.empty
      case Some(idx) => {
        var r:Int = idx
        var seq:Queue[(Int, Type)] = Queue.empty[(Int, Type)]
        actualParameterTypes.foreach(ty => {
          seq += (r, ty)
          r += ty.size
        })
        return seq.toSeq
      }
    }
  }

  //The number of total registers
  private var registerCount:Int = 0
  def setRegisterCount(count:Int) = registerCount = count
  def getRegisterCount() = registerCount


  //The position of thisRegister
  private var thisRegister : IL.Register = -1
  def getThisRegister(): Option[IL.Register] = {
    if(isStaticMethod()) return None
    if(thisRegister == -1){
      thisRegister = this.registerCount - getParameterCount();
    }
    return Some(thisRegister)
  }

  //The position of the register mapped to the first actual parameter
  private var firstActualParameterRegister = -1;
  def getFirstActualParameterRegister():Option[IL.Register] = {
    if (getActualParameterCount() == 0) return None
    if (firstActualParameterRegister == -1){
      firstActualParameterRegister = this.registerCount - getActualParameterCount();
    }
    return Some(firstActualParameterRegister)
  }

  //The number of local register = non-parameter, non-this registers
  def getLocalRegisterCount(): Int = {
    return registerCount - getParameterCount()
  }

  //def getLiveRegisterTable() = app.getLiveRegisterTableOf(this)


  def getQuantifiedName():String = _class.name + "::" + _name
  def className():String = _class.name
  def methodName():String = name
  def descriptor():String = _desc


  private var uninitializedCFG:CFG = new CFG(this)
  private var _cfg:CFG = null

  def getUninitializedCFG():CFG = uninitializedCFG

  def registerCFG() ={
    _cfg = uninitializedCFG
  }
  def cfg():CFG = _cfg


  def includingClass():ClassInfo = _class
  def includingApp():ApplicationInfo = app


  def exportToDot(pathParentDirectory:String) {
    Debug.println("Dottify:" + this.getQuantifiedName() + " " + this.descriptor())
    if (isAbstractMethod() ||  isNative()) return

    val dot: GraphViz =
      try
        cfg.exportToDot()
      catch{
        case e =>
           e.printStackTrace()
           throw new RuntimeException(e)
      }

    var dir: File = new File(pathParentDirectory + "/" + _class.name.replace('/', '_'))
    if(dir.exists() && !dir.isDirectory){
      throw new RuntimeException("File exist and not a directory : " + dir.getAbsolutePath)
    }
    dir.mkdirs()

    var file: File = new File(pathParentDirectory + "/" + _class.name.replace('/', '_') + "/" + name + "-" + descriptor().hashCode + ".dot")

    var fw = new FileWriter(file)
    fw.write(dot.getDotSource)
    fw.close()
  }

  private var instructionCount = 0;
  def increaseInstructionCount() = instructionCount += 1
  def getInstructionCount() = instructionCount
}

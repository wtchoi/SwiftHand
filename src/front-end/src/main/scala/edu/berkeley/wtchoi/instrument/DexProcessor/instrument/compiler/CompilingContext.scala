package edu.berkeley.wtchoi.instrument.DexProcessor.instrument.compiler

import edu.berkeley.wtchoi.instrument.DexProcessor.il.{IL, MethodInfo, BasicBlock}
import edu.berkeley.wtchoi.instrument.DexProcessor.instrument.RegisterRemapping
import edu.berkeley.wtchoi.instrument.DexProcessor.il.types.RegisterType

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 11/12/12
 * Time: 7:35 PM
 * To change this template use File | Settings | File Templates.
 */
class CompilingContext(_bb:BasicBlock, _mi:MethodInfo, _rr:RegisterRemapping, _isEntry:Boolean, _isInTryBlock:Boolean) {
  def this(mi:MethodInfo, rr:RegisterRemapping){
    this(null, mi, rr, false, false)
    enterSuperMode()
  }

  private var superMode = false

  def enterSuperMode()= superMode = true
  def isSuperMode():Boolean = superMode

  def isInTryBlock():Boolean = _isInTryBlock

  def targetBB():BasicBlock = _bb
  def rr():RegisterRemapping = _rr
  def isEntry():Boolean = _isEntry
  def handlingMethod():MethodInfo = _mi

  def getThisRegister():Option[Int] = rr().getThisRegister()

  def getRegisterTypeAtEntry(r:Int):RegisterType ={
    rr().getRegisterTypeAtEntry(targetBB, r)
  }

  def getRegisterTypeAtExit(r:Int):RegisterType = {
    rr().getRegisterTypeAtExit(targetBB, r)
  }

  def getAvailableRegsAtBeginning():Set[Int] = {
    if (superMode){
      var rset = Set.empty[Int]
      for (i <- 0 until rr().getRegisterCount()){
        if (rset != rr().getThisRegister())
          rset += i
      }
      return rset
    }
    else if (isEntry)
      rr.getFreeRegistersAtEntry(targetBB())
    else //TODO : WHAT IS THIS?
      rr.getFreeRegistersAtExit(targetBB())
  }
}

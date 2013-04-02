package edu.berkeley.wtchoi.instrument.DexProcessor.instrument

import edu.berkeley.wtchoi.instrument.DexProcessor.il.{ApplicationInfo, ClassInfo, MethodInfo}

abstract class InstrumentSpecification {
  def getCodeAssignment(mi:MethodInfo):CodeAssignment
  def getAdditionalClasses(ai:ApplicationInfo):Iterable[ClassDescriptor]
  def getAdditionalMethods(ci:ClassInfo):Iterable[MethodDescriptor]
}

package com.meituan.firefly.node

case class Function(functionType: Type, name: SimpleId, params: Seq[Field], throws: Option[Seq[Field]], comment: Option[String])

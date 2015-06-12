package com.meituan.firefly.node

/**
 * Created by ponyets on 15/5/29.
 */
case class Function(functionType: Type, name: SimpleId, params: Seq[Field] , throws: Option[Seq[Field]], comment:Option[String])

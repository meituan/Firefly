package com.meituan.firefly.node

/**
 * Created by ponyets on 15/5/20.
 */
sealed abstract class Type

case object OnewayVoid extends Type

case object Void extends Type

sealed abstract class BaseType extends Type

case object TBool extends BaseType

case object TByte extends BaseType

case object TI16 extends BaseType

case object TI32 extends BaseType

case object TI64 extends BaseType

case object TDouble extends BaseType

case object TString extends BaseType

case object TBinary extends BaseType

sealed abstract class ContainerType extends Type

case class MapType(keyType: Type, valueType: Type) extends ContainerType

case class SetType(typeParam: Type) extends ContainerType

case class ListType(typeParam: Type) extends ContainerType

case class IdentifierType(id: Identifier) extends Type


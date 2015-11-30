package com.meituan.firefly

class ConvertException(message: String) extends Exception(message)

class TypeNotFoundException(struct: String)(implicit exceptionContext: String) extends ConvertException(s"$exceptionContext: Struct $struct Not Found")

class IncludeNotFoundException(file: String)(implicit exceptionContext: String) extends ConvertException(s"$exceptionContext: Include $file Not Found")

class ValueTypeNotMatchException(expect: String, actual: String)(implicit exceptionContext: String) extends ConvertException(s"$exceptionContext: Type $expect expected but got $actual type value")

class ServiceNotFoundException(service: String)(implicit exceptionContext: String) extends ConvertException(s"$exceptionContext: Struct $service Not Found")

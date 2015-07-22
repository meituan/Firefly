package com.meituan.firefly

class ConvertException(message: String) extends Exception(message)

class StructNotFoundException(struct: String) extends ConvertException(s"Struct $struct Not Found")

class IncludeNotFoundException(file: String) extends ConvertException(s"Include $file Not Found")

class ValueTypeNotMatchException(expect: String, actual: String) extends ConvertException(s"Type $expect expected but got $actual type value")

class ServiceNotFoundException(service: String) extends ConvertException(s"Struct $service Not Found")

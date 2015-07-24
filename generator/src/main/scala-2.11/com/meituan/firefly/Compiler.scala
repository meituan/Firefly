package com.meituan.firefly

import java.io.File

case class Compiler(thriftFiles: List[File] = List(), output: File = new File("gen")) {

  def run(): Unit = {
    val generator = new Generator(output = output)
    thriftFiles.foreach {
      file =>
        val document = new ThriftParser(file.getParentFile).parseFile(file)
        generator(document)
    }
  }
}

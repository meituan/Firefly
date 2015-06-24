package com.meituan.firefly

import java.io.File

/**
 * Created by ponyets on 15/6/2.
 */
case class Compiler(thriftFiles: List[File] = List(), output: File = new File("gen")) {

  def run(): Unit = {
    val generator = new Generator(output = output)
    thriftFiles.foreach {
      file =>
        val document = new ThriftParser(file.getParentFile).parseFile(file)
        println(document)
        generator(document)
    }
  }
}

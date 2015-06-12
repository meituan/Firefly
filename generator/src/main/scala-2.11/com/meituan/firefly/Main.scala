package com.meituan.firefly

import java.io.File

import scopt.OptionParser

/**
 * Created by ponyets on 15/6/2.
 */
object Main {
  def main(args: Array[String]): Unit = {
    parser.parse(args, Compiler()).foreach(_.run())
  }

  val parser = new OptionParser[Compiler]("Firefly") {
    help("help") text ("prints this usage text")
    arg[File]("<file>...") unbounded() action { (file, c) =>
      c.copy(thriftFiles = file :: c.thriftFiles)
    }
  }
}

organization := "com.meituan.firefly"

name := "library"

version := "0.2.3"

scalaVersion := "2.11.6"

libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.3"

libraryDependencies += "com.squareup.okhttp" % "okhttp" % "2.4.0"

libraryDependencies += "io.reactivex" % "rxjava" % "1.0.15"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

libraryDependencies += "org.assertj" % "assertj-core" % "2.1.0" % Test

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.12" % Test

autoScalaLibrary := false

publishMavenStyle := true

crossPaths := false

javacOptions in (Compile, compile) ++= Seq("-source", "1.7", "-target", "1.7")

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

bintrayPackage := "com.meituan.firefly:library"

bintrayPackageLabels := Seq("thrift", "android")

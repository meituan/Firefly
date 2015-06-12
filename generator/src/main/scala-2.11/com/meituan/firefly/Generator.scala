package com.meituan.firefly

import java.io.{File, FileOutputStream, OutputStreamWriter}

import com.meituan.firefly.node._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.util.{FileResourceLoader, Resource}

import scala.collection.mutable.ListBuffer

/**
 * Created by ponyets on 15/6/8.
 */
class Generator(defaultNameSpace: String = "thrift", output: File = new File("gen")) extends (Document => Seq[File]) {
  val engine = new TemplateEngine()
  engine.resourceLoader = new FileResourceLoader {
    override def resource(uri: String): Option[Resource] = Some(Resource.fromURL(getClass.getResource(uri)))
  }
  engine.escapeMarkup = false

  def getNameSpace(doc: Document) = doc.headers.collectFirst { case NameSpace(scope, id) if scope == "java" || scope == "*" => id.fullName }.getOrElse(defaultNameSpace)

  def getNameSpaceFolder(nameSpace: String) = {
    val nameSpaceFolder = new File(output, nameSpace.replace('.', File.separatorChar))
    nameSpaceFolder.mkdirs()
    nameSpaceFolder
  }

  def writeFile(file: File, content: String): Unit = {
    val stream = new FileOutputStream(file)
    val writer = new OutputStreamWriter(stream, "UTF-8")
    try {
      writer.write(content)
    } finally {
      writer.close()
      stream.close()
    }
  }

  override def apply(doc: Document): Seq[File] = {
    val nameSpace = getNameSpace(doc)
    val nameSpaceFolder = getNameSpaceFolder(nameSpace)
    val baseMap = Map[String, Any]("package" -> nameSpace)
    val listBuf = ListBuffer[File]()

    val consts = doc.defs.collect { case x: Const => x }
    if (consts.nonEmpty) {
      val constsFile = new File(nameSpaceFolder, "Consts.java")
      writeFile(constsFile, engine.layout("/const.mustache", convertConsts(baseMap, consts, doc)))
      listBuf += constsFile
    }

    val structs = doc.defs.collect { case x: StructLike => x }
    listBuf ++= (for (struct <- structs) yield {
      val structFile = new File(nameSpaceFolder, struct.name.name + ".java")
      writeFile(structFile, engine.layout("/struct.mustache", convertStructLike(baseMap, struct, doc)))
      structFile
    })

    val enums = doc.defs.collect { case x: Enum => x }
    listBuf ++= (for (enum <- enums) yield {
      val enumFile = new File(nameSpaceFolder, enum.name.name + ".java")
      writeFile(enumFile, engine.layout("/enum.mustache", convertEnum(baseMap, enum, doc)))
      enumFile
    })

    val service = doc.defs.collect { case x: Service => x }
    listBuf ++= (for (service <- service) yield {
      val serviceFile = new File(nameSpaceFolder, service.name.name + ".java")
      writeFile(serviceFile, engine.layout("/service.mustache", convertService(baseMap, service, doc)))
      serviceFile
    })
    listBuf
  }

  def convertConsts(baseMap: Map[String, Any], consts: Seq[Const], doc: Document): Map[String, Any] = {
    val constsValue: Seq[Map[String, Any]] = for (const <- consts) yield convertConst(const, doc)
    baseMap + ("consts" -> constsValue)
  }

  def convertConst(const: Const, doc: Document): Map[String, Any] = {
    Map("fieldType" -> convertType(const.fieldType, doc),
      "name" -> const.name.fullName,
      "value" -> convertConstValue(const.fieldType, const.value, doc)
    )
  }

  def convertType(fieldType: Type, doc: Document): String = {
    fieldType match {
      case OnewayVoid | Void => "void"
      case t: BaseType => convertBaseType(t)
      case t: ContainerType => convertContainerType(t, doc)
      case t: IdentifierType => convertIdentifierType(t, doc)
    }
  }

  def convertBaseType(baseType: BaseType): String = {
    baseType match {
      case TBool => "Boolean"
      case TByte => "Byte"
      case TI16 => "Short"
      case TI32 => "Integer"
      case TI64 => "Long"
      case TDouble => "Double"
      case TString => "String"
      case TBinary => "byte[]"
    }
  }

  def convertContainerType(containerType: ContainerType, doc: Document) = {
    containerType match {
      case t: ListType =>
        val typeParam = convertType(t.typeParam, doc)
        s"List<$typeParam}>"
      case t: SetType =>
        val typeParam = convertType(t.typeParam, doc)
        s"Set<$typeParam>"
      case t: MapType =>
        val keyParam = convertType(t.keyType, doc)
        val valueParam = convertType(t.valueType, doc)
        s"Map<$keyParam, $valueParam>"
    }
  }

  def convertIdentifierType(idType: IdentifierType, doc: Document) = {
    idType.id match {
      case id: SimpleId => doc.defs.collectFirst { case struct: StructLike if struct.name == id => id.name }
        .orElse(doc.defs.collectFirst { case enum: Enum if enum.name == id => id.name })
        .getOrElse(throw new StructNotFoundException(id.fullName))
      case id: QualifiedId =>
        val includeDocument: Document = getInclude(doc, id.qualifier).document
        includeDocument.defs.collectFirst { case struct: StructLike if struct.name.name == id.name => id }
          .orElse(includeDocument.defs.collectFirst { case enum: Enum if enum.name.name == id.name => id })
          .map(getNameSpace(includeDocument) + "." + _.name)
          .getOrElse(throw new StructNotFoundException(id.fullName))
    }
  }

  def getInclude(doc: Document, fileName: String) = {
    doc.headers.collect {
      case i: Include => i
    }.find(i => i.file == fileName || i.file == fileName + ".thrift").getOrElse(throw new IncludeNotFoundException(fileName))
  }

  def convertConstValue(fieldType: Type, value: ConstValue, doc: Document): String = {
    value match {
      case v: Literal => "\"" + v.value + "\""
      case v: IntConstant => v.value.toString
      case v: DoubleConstant => v.value.toString
      case v: IdConstant => convertIdConstant(v, doc)
      case v: ConstList =>
        fieldType match {
          case ListType(tp) => "java.util.Arrays.asList(" + v.elems.map(convertConstValue(tp, _, doc)).mkString(", ") + ")"
          case SetType(tp) => "new java.util.HashSet(java.util.Arrays.asList(" + v.elems.map(convertConstValue(tp, _, doc)).mkString(", ") + "))"
          case _ => throw new ValueTypeNotMatchException(fieldType.toString, "list")
        }

      case v: ConstMap =>
        fieldType match {
          case MapType(keyType, valueType) =>
            val kvs = for (elem <- v.elems) yield "new java.util.AbstractMap.SimpleImmutableEntry(" + convertConstValue(keyType, elem._1, doc) + ", " + convertConstValue(valueType, elem._2, doc) + ")"
            "com.meituan.firefly.util.Maps.asMap(" + kvs.mkString(", ") + ")"
          case _ => throw new ValueTypeNotMatchException(fieldType.toString, "map")
        }

    }
  }

  def convertIdConstant(value: IdConstant, doc: Document): String = {
    value.id match {
      case id: SimpleId => "Consts." + id.name
      case id: QualifiedId => getNameSpace(getInclude(doc, id.qualifier).document) + ".Consts." + id.name;
    }
  }

  def convertStructLike(baseMap: Map[String, Any], structLike: StructLike, document: Document): Map[String, Any] = {
    baseMap ++ Map("name" -> structLike.name.fullName) ++ {
      structLike match {
        case x: Union => Map("isUnion" -> true)
        case x: ExceptionDef => Map("isException" -> true)
        case _ => Map()
      }
    } + ("doc" -> structLike.comment) +
      ("fields" -> {
        val idFilledFields = structLike.fields.filter(_.id.isDefined) ++ structLike.fields.filter(_.id.isEmpty).zipWithIndex.map(p => p._1.copy(Some(-1 - p._2)))
        for (field <- idFilledFields) yield convertField(field, document)
      })
  }

  def convertField(field: Field, document: Document): Map[String, Any] = Map(
    "doc" -> field.comment, "id" -> field.id,
    "required" -> field.requiredness.map(_ == Requiredness.Required).getOrElse(true),
    "fieldType" -> convertType(field.fieldType, document),
    "name" -> field.identifier.name,
    "value" -> field.value.map(convertConstValue(field.fieldType, _, document))
  )

  def convertEnum(baseMap: Map[String, Any], enum: Enum, document: Document): Map[String, Any] = {
    baseMap + ("doc" -> enum.comment) + ("name" -> enum.name.name) +
      ("elems" -> (if (enum.elems.isEmpty) Seq()
      else {
        val lastElem = enum.elems.last
        (for (elem <- enum.elems.dropRight(1)) yield Map("name" -> elem._1.name, "id" -> elem._2)) :+
          Map("name" -> lastElem._1.name, "id" -> lastElem._2, "last" -> true)
      }))
  }

  def convertService(baseMap: Map[String, Any], service: Service, document: Document): Map[String, Any] = {
    baseMap ++
      Map("doc" -> service.comment,
        "name" -> service.name.name,
        "parent" -> service.parent.map {
          case p: SimpleId => p.name
          case p: QualifiedId => {
            val includeDoc = getInclude(document, p.qualifier).document
            includeDoc.defs.collectFirst {
              case x: Service if x.name.name == p.name => getNameSpace(includeDoc) + "." + p.name
            }.getOrElse(throw new ServiceNotFoundException(p.fullName))
          }
        },
        "functions" -> (for (func <- service.functions) yield convertFunction(func, document))
      )
  }

  def convertFunction(function: Function, document: Document): Map[String, Any] = Map(
    "doc" -> function.comment,
    "oneway" -> (function.functionType == OnewayVoid),
    "exceptions" -> function.throws.map {
      exceptions =>
        def convertExceptionField(field: Field) = Map("id" -> field.id.get,
          "required" -> field.requiredness.map(Requiredness.Required == _).getOrElse(true),
          "fieldType" -> convertType(field.fieldType, document)
        )
        val idFilledExceptions = exceptions.filter(_.id.isDefined) ++ exceptions.filter(_.id.isEmpty).zipWithIndex.map(p => p._1.copy(Some(-1 - p._2)))
        idFilledExceptions match {
          case head :: tail => (convertExceptionField(head) + ("first" -> true)) :: tail.map(convertExceptionField)
          case _ => Seq()
        }
    }.getOrElse(Seq()),
    "funcType" -> (if (OnewayVoid == function.functionType) "void" else convertType(function.functionType, document)),
    "name" -> function.name.name,
    "params" -> {
      function.params match {
        case head :: tail => (convertField(head, document) + ("first" -> true)) :: tail.map(convertField(_, document))
        case _ => Seq()
      }
    }
  )
}

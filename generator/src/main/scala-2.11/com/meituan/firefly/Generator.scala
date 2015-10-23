package com.meituan.firefly

import java.io.{File, FileOutputStream, OutputStreamWriter}

import com.meituan.firefly.node._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.util.{FileResourceLoader, Resource}

import scala.collection.mutable.ListBuffer

/**
 * A generator is used to generate code from a specified thrift file.<br />
 * It use mustache template language.
 * @param defaultNameSpace default namespace if the thrift file does not specify one
 * @param output output dir for generated code
 */
class Generator(defaultNameSpace: String = "thrift", output: File = new File("gen")) extends ((Document, String) => Seq[File]) {
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

  /**
   * generates code from the Document.
   * @param doc
   * @return
   */
  override def apply(doc: Document, filename: String): Seq[File] = {
    implicit val exceptionContext = s"@file $filename"
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
      writeFile(structFile, engine.layout("/struct.mustache", convertStructLike(baseMap, struct, doc)(exceptionContext + s" @${struct.getClass.getSimpleName} ${struct.name.fullName}")))
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
      writeFile(serviceFile, engine.layout("/service.mustache", convertService(baseMap, service, doc)(exceptionContext + s" @service ${service.name.fullName}")))
      serviceFile
    })
    listBuf
  }

  /**
   * converts a Const Node in the Document to a Map, which is applied to const.mustache template.
   * @param baseMap
   * @param consts
   * @param doc
   * @return
   */
  def convertConsts(baseMap: Map[String, Any], consts: Seq[Const], doc: Document)(implicit exceptionContext: String): Map[String, Any] = {
    val constsValue: Seq[Map[String, Any]] = for (const <- consts) yield convertConst(const, doc)(exceptionContext + s" @const ${const.name.fullName}")
    baseMap + ("consts" -> constsValue)
  }

  def convertConst(const: Const, doc: Document)(implicit exceptionContext: String): Map[String, Any] = {
    Map("fieldType" -> convertType(const.fieldType, doc),
      "name" -> const.name.fullName,
      "value" -> convertConstValue(const.fieldType, const.value, doc)
    )
  }

  def convertType(fieldType: Type, contextDoc: Document, targetDoc: Document)(implicit exceptionContext: String): String = {
    fieldType match {
      case OnewayVoid | Void => "void"
      case t: BaseType => convertBaseType(t)
      case t: ContainerType => convertContainerType(t, contextDoc, targetDoc)
      case t: IdentifierType => convertIdentifierType(t, contextDoc, targetDoc)
    }
  }

  def convertType(fieldType: Type, doc: Document)(implicit exceptionContext: String): String = convertType(fieldType, doc, doc)

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

  def convertContainerType(containerType: ContainerType, contextDoc: Document, targetDoc: Document)(implicit exceptionContext: String) = {
    containerType match {
      case t: ListType =>
        val typeParam = convertType(t.typeParam, contextDoc, targetDoc)
        s"List<$typeParam>"
      case t: SetType =>
        val typeParam = convertType(t.typeParam, contextDoc, targetDoc)
        s"Set<$typeParam>"
      case t: MapType =>
        val keyParam = convertType(t.keyType, contextDoc, targetDoc)
        val valueParam = convertType(t.valueType, contextDoc, targetDoc)
        s"Map<$keyParam, $valueParam>"
    }
  }

  def convertIdentifierType(idType: IdentifierType, contextDoc: Document, targetDoc: Document)(implicit exceptionContext: String): String = {

    def findNamedType(name: String, contextDoc: Document, targetDoc: Document) = {
      contextDoc.defs.collectFirst { case struct: StructLike if struct.name.name == name => if (contextDoc == targetDoc) name else getNameSpace(contextDoc) + "." + name }
        .orElse(contextDoc.defs.collectFirst { case enum: Enum if enum.name.name == name => if (contextDoc == targetDoc) name else getNameSpace(contextDoc) + "." + name })
        .orElse(contextDoc.defs.collectFirst { case typedef: Typedef if typedef.name.name == name => convertType(typedef.definitionType, contextDoc, targetDoc) })
        .getOrElse(throw new StructNotFoundException(idType.id.fullName))
    }
    idType.id match {
      case id: SimpleId => findNamedType(id.name, contextDoc, targetDoc)
      case id: QualifiedId =>
        val includeDocument: Document = getInclude(contextDoc, id.qualifier).document
        findNamedType(id.name, includeDocument, targetDoc)
    }
  }


  def getInclude(doc: Document, fileName: String)(implicit exceptionContext: String) = {
    doc.headers.collect {
      case i: Include => i
    }.find(i => i.file == fileName || i.file == fileName + ".thrift").getOrElse(throw new IncludeNotFoundException(fileName))
  }

  def convertConstValue(fieldType: Type, value: ConstValue, doc: Document)(implicit exceptionContext: String = ""): String = {
    value match {
      case v: Literal =>
        fieldType match {
          case TString => "\"" + v.value + "\""
          case _ => throw new ValueTypeNotMatchException(fieldType.toString, "string")
        }
      case v: BoolConstant =>
        fieldType match {
          case TBool => v.value.toString
          case _ => throw new ValueTypeNotMatchException(fieldType.toString, "bool")
        }
      case v: IntConstant =>
        fieldType match {
          case TI16 => "(short) " + v.value.toString
          case TI32 => v.value.toString
          case TI64 => v.value.toString + "l"
          case TDouble => "(double) " + v.value.toString
          case _ => throw new ValueTypeNotMatchException(fieldType.toString, "int")
        }
      case v: DoubleConstant =>
        fieldType match {
          case TDouble => v.value.toString
          case _ => throw new ValueTypeNotMatchException(fieldType.toString, "double")
        }
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

  def convertIdConstant(value: IdConstant, doc: Document)(implicit exceptionContext: String): String = {
    value.id match {
      case id: SimpleId => "Consts." + id.name
      case id: QualifiedId => getNameSpace(getInclude(doc, id.qualifier).document) + ".Consts." + id.name;
    }
  }

  /**
   * converts a Struct/Union/Exception Node in the Document to a Map, which is applied to struct.mustache template.
   * @param baseMap
   * @param structLike
   * @param document
   * @return
   */
  def convertStructLike(baseMap: Map[String, Any], structLike: StructLike, document: Document)(implicit exceptionContext: String): Map[String, Any] = {
    baseMap ++ Map("name" -> structLike.name.fullName) ++ {
      structLike match {
        case x: Union => Map("isUnion" -> true)
        case x: ExceptionDef => Map("isException" -> true)
        case _ => Map()
      }
    } + ("doc" -> structLike.comment) +
      ("fields" -> {
        for (field <- fillFieldsIds(structLike.fields)) yield convertField(field, document)(exceptionContext + s" @field ${field.identifier.fullName}")
      })
  }

  def fillFieldsIds(fields: Seq[Field]) = fields.filter(_.id.isDefined) ++ fields.filter(_.id.isEmpty).zipWithIndex.map(p => p._1.copy(Some(-1 - p._2)))

  def convertField(field: Field, document: Document)(implicit exceptionContext: String): Map[String, Any] = Map(
    "doc" -> field.comment, "id" -> field.id,
    "required" -> field.requiredness.map(_ == Requiredness.Required).getOrElse(false),
    "fieldType" -> convertType(field.fieldType, document),
    "name" -> field.identifier.name,
    "value" -> field.value.map(convertConstValue(field.fieldType, _, document))
  )

  /**
   * converts a Enum Node in the Document to a Map, which is applied to enum.mustache template.
   * @param baseMap
   * @param enum
   * @param document
   * @return
   */
  def convertEnum(baseMap: Map[String, Any], enum: Enum, document: Document): Map[String, Any] = {
    baseMap + ("doc" -> enum.comment) + ("name" -> enum.name.name) +
      ("elems" -> (if (enum.elems.isEmpty) Seq()
      else {
        val lastElem = enum.elems.last
        (for (elem <- enum.elems.dropRight(1)) yield Map("name" -> elem._1.name, "id" -> elem._2)) :+
          Map("name" -> lastElem._1.name, "id" -> lastElem._2, "last" -> true)
      }))
  }

  /**
   * converts a Service Node in the Document to a Map, which is applied to service.mustache template.
   * @param baseMap
   * @param service
   * @param document
   * @return
   */
  def convertService(baseMap: Map[String, Any], service: Service, document: Document)(implicit exceptionContext: String): Map[String, Any] = {
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
        "functions" -> (for (func <- service.functions) yield convertFunction(func, document)(exceptionContext + s" @function ${func.name.fullName}"))
      )
  }

  def convertFunction(function: Function, document: Document)(implicit exceptionContext: String): Map[String, Any] = Map(
    "doc" -> function.comment,
    "oneway" -> (function.functionType == OnewayVoid),
    "exceptions" -> function.throws.map {
      exceptions =>
        def convertExceptionField(field: Field) = Map("id" -> field.id.get,
          "required" -> field.requiredness.map(Requiredness.Required == _).getOrElse(false),
          "fieldType" -> convertType(field.fieldType, document),
          "name" -> field.identifier.name
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
      fillFieldsIds(function.params) match {
        case head :: tail => (convertField(head, document)(exceptionContext + s" @param ${head.identifier.fullName}") + ("first" -> true)) :: tail.map(convertField(_, document))
        case _ => Seq()
      }
    }
  )
}

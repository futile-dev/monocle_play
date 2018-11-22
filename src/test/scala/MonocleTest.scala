import java.io.File

import monocle.function.Index._
import monocle.{Optional, Prism}
import org.scalatest.FlatSpec
import spray.json.{JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonFormat, _}
import transformers.AnyJsonFormat

import scala.io.Source

object transformers {

  implicit object AnyJsonFormat extends RootJsonFormat[Any] {
    def write(x: Any) = x match {
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
    }

    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
    }
  }

}

// super noddy play around to prove I can do what I need to soon.
class MonocleTest extends FlatSpec {

  //Using spray still as the AST matches what I needed.
  //I would like to explore transforming other file formats into AST format...
  val jString = Prism[JsValue, String] { case JsString(s) => Some(s); case _ => None }(JsString.apply)
  val jNumber = Prism[JsValue, BigDecimal] { case JsNumber(n) => Some(n); case _ => None }(JsNumber.apply)
  val jBoolean = Prism[JsValue, Boolean] { case JsBoolean(b) => Some(b); case _ => None }(JsBoolean.apply)
  val jArray = Prism[JsValue, Vector[JsValue]] { case JsArray(a) => Some(a); case _ => None }(JsArray.apply)
  val jObject = Prism[JsValue, Map[String, JsValue]] { case JsObject(m) => Some(m); case _ => None }(JsObject.apply)


  "Initial monocle test" should "pass path and get content" in {
    var basicJsonUrl = getClass.getClassLoader.getResource("basic_json_example.json").toURI
    val basicJsonFile = new File(basicJsonUrl)

    val basicJson = Source.fromFile(basicJsonFile).mkString

    val basicJsonAst = basicJson.parseJson

    val pathArray = pathToArray("$.test_object.test_nested_object.test_string")

    val output = getValueFromPathArray(pathArray, jObject.asOptional, basicJsonAst)

    assert(output == "string 2")
  }

  //This will need some more work
  def pathToArray(path: String): Array[String] = {
    path.replace("..", ".+.").replaceFirst("\\$.", "").split("\\.")
  }

  //not sure recursion is right way but works for now
  def getValueFromPathArray(pathArray: Array[String], optional: Optional[JsValue, Map[String, JsValue]], jsonAst: JsValue): Any = {

    if (pathArray.size < 2) {
      (optional composeOptional index(pathArray.head)).getOption(jsonAst).get.convertTo[Any]
    }
    else {
      //Will need to also check if head is array and use index(n)
      val head = pathArray.head
      val tail = pathArray.splitAt(1)._2

      //worth noting that if it's a plus "+" (recursive decent) then I think index does that by default (which might be a problem)
      val nextPrism = (optional composeOptional index(head) composePrism jObject)
      getValueFromPathArray(tail, nextPrism, jsonAst)
    }
  }

}

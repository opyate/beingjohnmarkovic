package models

import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._

case class Corpus(id: Option[Long] = None, datum: String)
case class OK(msg: String, numberOfRowsInserted: Int)

object JsonProtocol extends DefaultJsonProtocol {
  implicit val corpusFormat = jsonFormat2(Corpus.apply)
  implicit val idFormat = jsonFormat2(OK.apply)
}

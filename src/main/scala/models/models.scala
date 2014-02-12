package models

import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import java.util.UUID

case class Corpus(id: Option[Int] = None, text: String)

object JsonProtocol extends DefaultJsonProtocol {
  implicit val corpusFormat = jsonFormat2(Corpus.apply)
}

package api

import scala.concurrent.ExecutionContext
import spray.routing.Directives
import spray.http.MediaTypes.{ `application/json` }
import akka.actor.ActorRef
import akka.util.Timeout
import spray.httpx.SprayJsonSupport

import scala.concurrent.duration.Duration
import spray.routing.HttpService
import spray.routing.authentication.BasicAuth
import spray.routing.directives.CachingDirectives._
import spray.httpx.encoding._
import scala.reflect.ClassTag

import models._
import models.JsonProtocol._

class SourceService(source: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with SprayJsonSupport {

  import actors.SourceActor._
  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
  
  val route = rejectEmptyResponse {
    pathPrefix("api") {
      path("corpus") {
        post {
          entity(as[Corpus]) { corpus =>
            complete {
              (source ask corpus).mapTo[Option[OK]]
            }
          }
        }
      } ~ path("sentence" / Segment / Segment) { (word1, word2) =>
        get {
          respondWithMediaType(`application/json`) {
            complete {
              "Here we go!"
            }
          }
        }
      }
    }
  }
}

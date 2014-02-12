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

import models._
import models.JsonProtocol._

class SourceService(source: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with SprayJsonSupport {

  import actors.SourceActor._
  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
  
  val route = {
    pathPrefix("api") {
      path("corpus") {
        post {
          decompressRequest() {
            entity(as[Corpus]) { corpus =>
              detach() {
                complete {
                  source ! corpus
                  corpus
                }
              }
            }
          }
        }
      }
    }
  }
}

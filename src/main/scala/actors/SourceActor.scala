package actors

import akka.actor.Actor
import akka.util.Timeout
import akka.actor.Props
import akka.actor.ActorLogging
import models.Corpus

object SourceActor {
  
  def props(): Props = Props(new SourceActor())
}

class SourceActor() extends Actor with ActorLogging {

  import SourceActor._
  import scala.concurrent.duration._
  import context.dispatcher
  import akka.pattern.pipe
  
  implicit val timeout = Timeout(2.seconds)
  
  def receive: Receive = {
    case corpus: Corpus => {
      log.info("Received corpus {}", corpus)
    }
    case x => log.info("Received unknown {}", x)
  }
}

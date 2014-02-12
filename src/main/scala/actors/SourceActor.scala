package actors

import akka.actor.Actor
import akka.util.Timeout
import akka.actor.Props
import akka.actor.ActorLogging
import models._

object SourceActor {
  
  def props(): Props = Props(new SourceActor())
}

class SourceActor() extends Actor with ActorLogging {

  import SourceActor._
  import scala.concurrent.duration._
  import context.dispatcher
  import akka.pattern.pipe
  
  implicit val timeout = Timeout(2.seconds)
  
  val markov = context.actorOf(Props[MarkovActor], "markov")
  
  def receive: Receive = {
    case corpus: Corpus => {
      log.info("Received corpus {}", corpus)
      val numberOfNewRows = models.dao.add(corpus)
      markov ! corpus
      sender ! Some(OK("ok", numberOfNewRows))
    }
    case x => log.info("Received unknown {}", x)
  }
}

package actors

import com.redis.RedisClient
import akka.actor.Actor
import akka.util.Timeout
import models.Corpus
import core.Markov

object MarkovActor {
  /**
   * For 'leaders', get a sentence with at least 'leaders.size' + 'n' tokens.
   * E.g. leaders = "Once upon"
   *      n = 7
   * ...might result in: "Once upon a time there hacked a young lad."
   * 
   * If a punctuation mark is encountered before the limit is reached, start a new sentence until punctuation is reached.
   * 
   * This might then result in:
   *  1    2    3 4    5       6         7    8       9   10  +
   * "Once upon a time nothing happened. That sucked, and the lad decided to do something about it."
   */
  case class Get(leaders: String, n: Int) 
}
class MarkovActor extends Actor {
  
  import MarkovActor._
  import scala.concurrent.duration._
  import context.dispatcher
  
  implicit val timeout = Timeout(2.seconds)

  // Redis client setup
  val db = RedisClient("localhost", 6379)
 
  def receive: Receive = {
    case corpus: Corpus => buildChain(corpus)
    case get @ Get(leaders: String, n: Int) =>
      sender ! sentence(get)
  }
  
  private [this] def sentence(params: Get): String = {
    "What a happy sentence."
  }
  
  private [this] def buildChain(corpus: Corpus) {
    // group the words
    val groups = Markov.groups(3, corpus.datum.split("\\s+"))
    
    // adjust score each grouping
    groups foreach { case (pair, word) => {
      bump(pair.mkString(" "), word)
    }}
  }
  
  /**
   * Ranks a 'next' word after a 'phrase', e.g. for
   * phrase "Markov is"
   * word "fun"
   * it will bump the score for "fun" by 1.
   */
  private [this] def bump(phrase: String, next: String) {
    db.zincrby(s"${global.ns}:$phrase", 1, next)
  }
}
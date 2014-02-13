package actors

import com.redis.RedisClient
import akka.actor.Actor
import akka.util.Timeout
import models.Corpus
import core.Markov
import scala.concurrent.Future
import akka.actor.ActorLogging
import scala.util.Random
import scala.util.Success
import scala.util.Failure
import scala.concurrent.future

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
  case class Get(leaders: List[String], n: Int) 
}
class MarkovActor extends Actor with ActorLogging {
  
  type WordAndScore = (String, Double)
  
  import MarkovActor._
  import scala.concurrent.duration._
  import context.dispatcher
  
  implicit val timeout = Timeout(2.seconds)

  // Redis client setup
  val db = RedisClient("localhost", 6379)
 
  def receive: Receive = {
    case corpus: Corpus => buildChain(corpus)
    case get: Get =>
      sender ! generate(get)
  }
  
  /**
   * We choose the next word at random, but 'score' weights it.
   */
  private [this] def generate(params: Get): Future[String] = {
    gen(params.leaders, params.leaders.size).map(x => x.mkString(" "))
  }
  
  /**
   * 'seed' is a phrase with which we start the new random sentence.
   */
  private [this] def gen(acc: List[String], leaderSize: Int): Future[List[String]] = {
    val seed = acc.takeRight(leaderSize).mkString(" ")
    log.warning(s"SEED $seed")
    val futureZRange = db.zrangeWithScores[String](s"${global.ns}:${seed}", 0, -1)
    
    for {
      // a list of string/double tuples, which will be empty if there aren't any more words coming after the seed.
      list <- futureZRange
    } yield {
      if (list.isEmpty) {
        // terminate here; return what we have.
        // TODO possibly check if 'seed' ends with punctuation, and add some if necessary.
        log.warning("TERMINATING")
        acc
      } else {
        
        // debug
        if (false) {
          val cat = for {
            (next, score) <- list
          } yield s"word: $next, with score $score"
          val available = cat.mkString(" ")
          log.debug(s"Markov gave us $available")
        }
      
        // randomly choose next word here
        getNext(list) match {
          case Some(next) =>
            // TODO fix this hack. We don't want to wait. Find a way to collapse the below Future into the calling one.
            scala.concurrent.Await.result(gen(acc :+ next, leaderSize),  5.seconds)
          case None => acc
        }
      }
    }
  }
  
  /**
   * If List(("jock", 1.0), ("geek", 5.0)), we want the 'geek' to be chosen 5 times more 
   * frequently than the 'jock'.
   * 
   * Instead of creating a new list which explodes the values according to their weights, e.g.
   * List(jock, geek, geek, geek, geek, geek) and choosing by random index, 
   * ...we prepare a list of intervals that cover 0 to sum(weights).
   * 
   * This will require more processing, but won't sucker punch memory, as some words might appear *many* times.
   * 
   * Here's how we do it:
   * 
   * List(("a", 1.0), ("b", 5.0), ("c", 10.0))

1.0
5.0
10.0

Calculate intervals:

1, just the first element
6 (previous plus next weight, 1 + 5)
16 (previous plus next weight, 6 + 10)

[1, 6, 16]

|-|-----|----------|
0 1     6          16

random between 0 and 16
dropWhile( i < rnd).head


E.g. for rnd 12

              12
|-|-----|-----*----|
0 1     6          16

it will drop 1 and 6, leave 16 for the taking.
   */
  private [this] def getNext(list: List[WordAndScore]): Option[String] = {
    if (list.isEmpty) {
      None
    } else {
      val intervals = list.foldLeft((0.0, List.empty[WordAndScore])){ case (acc, wordAndScore) => {
        val adjusted: Double = acc._1 + wordAndScore._2
        val ongoing: List[WordAndScore] = acc._2
        val folded: List[WordAndScore] = List((wordAndScore._1, adjusted))
        (adjusted, ongoing ++ folded)
      }}._2
    
      val rnd = Random.nextInt(intervals.last._2.toInt).toDouble
      Some(intervals.dropWhile(pair => pair._2  < rnd).head._1)
    }
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
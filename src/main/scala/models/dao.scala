package models

import scala.slick.driver.PostgresDriver.simple._

// Definition of the corpus table
class Corpora(tag: Tag) extends Table[Corpus](tag, "corpora") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def datum = column[String]("datum")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id.?, datum) <> (Corpus.tupled, Corpus.unapply)
}

object dao {
  
  // 
  val corpora = TableQuery[Corpora]

  val dbUser = System.getenv("MYSQL_USER")
  val dbPassword = System.getenv("MYSQL_PASSWD")
  val dbHost = System.getenv("MYSQL_HOST")
  val dbName = System.getenv("MYSQL_DBNAME")
  val dbPort = System.getenv("MYSQL_PORT")
  val db: Database = Database.forURL(s"jdbc:mysql://$dbHost:$dbPort/$dbName?user=$dbUser&password=$dbPassword", driver = "com.mysql.jdbc.Driver")
  
  def add(corpus: Corpus): Int = {
    db withTransaction { implicit session: Session =>
      corpora.insert(corpus)
    }
  }
  
}
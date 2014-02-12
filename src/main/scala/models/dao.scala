package models

import scala.slick.driver.PostgresDriver.simple._

class dao {
  

  val suppliers = TableQuery[Corpus]

  val db: Database = Database.forURL("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
  
}
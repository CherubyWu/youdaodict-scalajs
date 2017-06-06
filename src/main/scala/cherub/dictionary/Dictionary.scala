package cherub.dictionary

/**
  * Created by cherub on 17-4-1.
  */
trait Dictionary {
  type URL = String

  def query(word: String)(callback: Option[QueryResponse] => Unit): Unit
  def wordURL(word: String): URL
  def imageSearchURL(word: String): URL
  def usPhoneticURL(word: String): URL
  def ukPhoneticURL(word: String): URL
}

case class QueryResponse(queryWord: String, explains: Seq[String], phonetic: Phonetic)

case class Phonetic(us: Option[String], uk: Option[String])

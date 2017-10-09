package cherub.dictionary.view

import org.scalajs.jquery.{JQuery, jQuery => $}

class View {
  val htmlClassPrefix: String = "_dict"

  def getByClass(cls: String): JQuery = $(cssClass(cls))
  def getByID(id: String): JQuery = $(cssID(id))

  def htmlClass(s: String): String = s"$htmlClassPrefix-$s"
  def htmlID(s: String): String = htmlClass(s)
  def cssClass(s: String): String = "." + htmlClass(s)
  def cssID(s: String): String = "#" + htmlID(s)
}

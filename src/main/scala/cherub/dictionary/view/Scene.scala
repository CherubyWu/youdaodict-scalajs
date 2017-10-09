package cherub.dictionary.view

import cherub.dictionary.{Dictionary, QueryResponse}
import org.scalajs.dom.{Element, window}
import org.scalajs.jquery.{JQuery, jQuery => $}

/**
  * Created by cherub on 17-4-2.
  */
abstract class Scene extends View {
  def content: JQuery

  def startLoad(): this.type
  def stopLoad(): this.type
}

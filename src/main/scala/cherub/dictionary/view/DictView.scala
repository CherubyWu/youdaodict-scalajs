package cherub.dictionary.view

import cherub.dictionary.{Dictionary, QueryResponse}
import org.scalajs.dom.{Element, window}
import org.scalajs.jquery.{JQuery, jQuery => $}

/**
  * Created by cherub on 17-4-2.
  */
abstract class DictView {
  val htmlClassPrefix: String = "dict"
  // At search button clicked, send search line value to function, default do nothing
  var searchCallback: String => Unit = { _ => () }


  def showWinAt(x: Double, y: Double): this.type
  def showWinAt(x: Double, y: Double, callback: () => Unit): this.type
  def startLoad(): this.type
  def stopLoad(): this.type
  def showQueryInWin(dict:Dictionary, queryResponse: QueryResponse): this.type
  def before_query(dict: Dictionary, word: String): this.type // 预先设置无需查询信息

  def removeOldWin(): this.type = {
   win.fadeOut(500, () => win.remove())
    this
  }
  def hideOldWin(): this.type = {
    win.fadeOut()
    this
  }

  def toggle(): JQuery = win.fadeToggle()
  def dictWin: Element = win(0)
  def win: JQuery = getByClass("win")
  def getByClass(cls: String): JQuery = $(cssClass(cls))
  def pointInWin(x: Double, y: Double): Boolean = {
    val rect = dictWin.getBoundingClientRect
    val rectLeft = rect.left + window.pageXOffset
    val rectTop = rect.top + window.pageYOffset

    (rectLeft < x && x < rectLeft + rect.width) &&
      (rectTop < y && y < rectTop + rect.height)
  }

  def htmlClass(s: String): String = s"$htmlClassPrefix-$s"
  def htmlID(s: String): String = htmlClass(s)
  def cssClass(s: String): String = "." + htmlClass(s)
  def cssID(s: String): String = "#" + htmlID(s)
}

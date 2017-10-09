package cherub.dictionary.view

import org.scalajs.jquery.{JQuery, jQuery => $, JQueryEventObject}
import org.scalajs.dom.{Element, window, document}

import cherub.js.Implicits._

class Window extends View {
  // create window node
  $("<div/>").
      hide().
      addClass(htmlClass("win")).
      appendTo(document.body)

  private val titleBar =
    $("<div/>").
      addClass(htmlClass("title-bar")).
      appendTo(win)

  private val title =
    $("<span/>").
      addClass(htmlClass("title")).
      html("Dictionary").
      appendTo(titleBar)

  private val content: JQuery =
    $("<div/>").
      addClass(htmlClass("content")).
      appendTo(win)

  initEvents()
  initCSS()

  def showWinAt(x: Double, y: Double): this.type =
    showWinAt(x, y, () => ())

  def showWinAt(x: Double, y: Double, callback: () => Unit): this.type = {
    print("show at: ")
    win.
      fadeOut { () =>
        win.moveTo(x, y)
        callback()
      }.
      fadeIn()
    this
  }
  def removeOldWin(): this.type = {
   win.fadeOut(500, () => win.remove())
    this
  }
  def hideOldWin(): this.type = {
    win.fadeOut()
    this
  }

  def autoMove() {
    val rect = dictWin.getBoundingClientRect()
    val rectPageLeft = win.css("left").stripSuffix("px").toDouble
    val rectPageTop = win.css("top").stripSuffix("px").toDouble
    val winX = if (rect.left + rect.width >= window.innerWidth)
      rectPageLeft - (rect.left + rect.width - window.innerWidth + 20)
    else rectPageLeft
    val winY = if (rect.top + rect.height >= window.innerHeight)
      rectPageTop - (rect.top + rect.height - window.innerHeight + 20)
    else rectPageTop
    win.moveTo(winX, winY)
  }

  def replaceScene(scene: Scene): this.type = {
    clear().content.append(scene.content)
    this
  }

  def toggle(): JQuery = win.fadeToggle()
  def dictWin: Element = win(0)
  def win: JQuery = getByClass("win")
  def pointInWin(x: Double, y: Double): Boolean = {
    val rect = dictWin.getBoundingClientRect
    val rectLeft = rect.left + window.pageXOffset
    val rectTop = rect.top + window.pageYOffset

    (rectLeft < x && x < rectLeft + rect.width) &&
      (rectTop < y && y < rectTop + rect.height)
  }

  def clear(): this.type = {
    content.html("")
    this
  }

  private def initEvents(): Unit = {
    var mouseDown = false
    var winX = 0.0
    var winY = 0.0
    var mouseX = 0.0
    var mouseY = 0.0
    titleBar.mousedown { e =>
      titleBar.css("cursor", "move")
      mouseDown = true
      winX = win.css("left").stripSuffix("px").toDouble
      winY = win.css("top").stripSuffix("px").toDouble
      mouseX = e.pageX
      mouseY = e.pageY
    }

    def mouseUp(e: JQueryEventObject): Unit = {
      titleBar.css("cursor", "default")
      mouseDown = false
    }

    titleBar.mouseup(mouseUp _)
    titleBar.mouseleave(mouseUp _)

    titleBar.mousemove { e =>
      if (mouseDown) {
        val offsetX = e.pageX - mouseX
        val offsetY = e.pageY - mouseY
        win.moveTo(winX + offsetX, winY + offsetY)
      }
    }
  }

  private def initCSS() {
    val backgroubndColor = "lightgray"
    $("<style/>").
      appendTo(document.body).
      html(
      s"""
        |.${win.attr("class")} {
        |  padding: 0;
        |  background: $backgroubndColor;
        |  display: none;
        |  position: absolute;
        |  border-radius: 5px;
        |  border: 1px solid lightslategrey;
        |  box-shadow: 0 3px 4px rgba(0, 0, 0, 0.2);
        |  z-index: 99999;
        |}
        |
        |.${titleBar.attr("class")} {
        |  background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, darkgray), color-stop(1, gray));
        |  background:-moz-linear-gradient(top, darkgray 5%, gray 100%);
        |  background:-webkit-linear-gradient(top, darkgray 5%, gray 100%);
        |  background:-o-linear-gradient(top, darkgray 5%, gray 100%);
        |  background:-ms-linear-gradient(top, darkgray 5%, gray 100%);
        |  background:linear-gradient(to bottom, darkgray 5%, gray 100%);
        |  background-color: darkgray;
        |  padding: 5px 0;
        |  margin: 0;
        |  height: 25px;
        |  text-align: center;
        |  cursor: default;
        |}
        |
        |.${title.attr("class")} {
        |  font-size: 15px;
        |  font-weight: bold;
        |  letter-spacing: 1px;
        |  color: snow;
        |  cursor: inherit;
        |  margin: 0;
        |}
        |
        |.${win.attr("class")} hr {
        |  clear: both;
        |}
        |
        |.${win.attr("class")} hr:first-of-type {
        |  margin-top: 3px;
        |  margin-bottom: 0.7em;
        |}
        |
        |.${win.attr("class")} hr:last-of-type {
        |  margin-top: 0.7em;
        |}
        |
      """.stripMargin)
  }
}

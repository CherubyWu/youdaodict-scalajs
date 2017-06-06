package cherub.dictionary.view
import cherub.dictionary.{Dictionary, QueryResponse}
import cherub.js.Implicits._
import org.scalajs.dom.raw.{HTMLAudioElement, MouseEvent}
import org.scalajs.dom.{document, window}
import org.scalajs.jquery.{JQuery, JQueryEventObject, jQuery => $}

/**
  * Created by cherub on 17-5-31.
  */
object YoudaoView extends {
  override val htmlClassPrefix: String = "youdao"
} with DictView {

  override def showWinAt(x: Double, y: Double): YoudaoView.this.type = {
    win
      .fadeOut(() => win.moveTo(x, y))
      .fadeIn()
    this
  }

  override def showQueryInWin(dict: Dictionary, queryResponse: QueryResponse): this.type = {
    val word = queryResponse.queryWord

    phoneticUK.html(
      queryResponse.phonetic.uk.map(p =>
        if (queryResponse.phonetic.us.isEmpty)
          s"[$p]"
        else
          s"英:[$p]"
      ).getOrElse(""))
    phoneticUS.html(queryResponse.phonetic.us.map(p => s"美:[$p]").getOrElse(""))
    if (queryResponse.phonetic.uk.isEmpty && queryResponse.phonetic.us.isEmpty)
      phoneticUK.html("&#9658")
    phoneticUKAudio.src = dict.ukPhoneticURL(word)
    phoneticUSAudio.src = dict.usPhoneticURL(word)

    explainsList.html("")
    for (text <- renderExplains(queryResponse.explains)) {
      $("<li/>").
        addClass(htmlClass("explains-item")).
        html(text).
        appendTo(explainsList)
    }

    autoMove()

    this
  }

  override def before_query(dict: Dictionary, word: String): this.type = {
    searchLine.value(word)
    queryWord.
      html(limitLength(word, 15)).
      attr("href", dict.wordURL(word))
    imageSearchLink.attr("href", dict.imageSearchURL(word))
    this
  }

  private var loadingBlink: Int = 0

  override def startLoad(): YoudaoView.this.type = {
    stopLoad() // 停止之前未停止的加载

    explainsList.html("")
    loading.html("查询中,请等待...").show()

    var flag = -1
    val changeSpeed = 0.05
    def currentOpacity = loading.css("opacity").toDouble
    loadingBlink = window.setInterval({ () =>
      loading.css("opacity", currentOpacity + changeSpeed * flag)
      if (currentOpacity >= 1 || currentOpacity <= 0.1)
        flag = -flag
    }, 100)
    this
  }

  override def stopLoad(): this.type = {
    loading.hide()
    window.clearInterval(loadingBlink)
    this
  }

  private def autoMove() {
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

  private def renderExplains(arr: Seq[String]): Seq[String] = {
    val limitLen = 20
    for (s <- arr) yield {
      val a = s.split(" ", 2)
      if (a.length == 2 && a(0).endsWith("."))
        s"""
           <span class="${htmlClass("word-type")} ${htmlClass("word-type-" + a(0).stripSuffix("."))}">${a(0)}</span>
           <span class="${htmlClass("explain")}">${limitLengthWithNewLine(a(1), limitLen)}</span>
          """
      else
        limitLengthWithNewLine(a(0), limitLen)
    }
  }

  private def limitLength(s: String, len: Int): String = {
    if (s.length > len)
      s.slice(0, len-3) + "..."
    else
      s
  }

  private def limitLengthWithNewLine(s: String, len: Int, html: Boolean = true): String = {
    val newLine = if (html) "<br/>" else "\n"
    if (s.length > len) {
      val (a, b) = s.splitAt(len)
      s"$a$newLine${limitLengthWithNewLine(b, len)}"
    } else s
  }

  override val win: JQuery =
    $("<div/>").
      hide().
      addClass(htmlClass("win")).
      appendTo(document.body)

  private val titleBar =
    $("<div/>").
      addClass(htmlClass("title-bar")).
      appendTo(win)

  private val title =
    $("<h3>").
      addClass(htmlClass("title")).
      html("Dictionary").
      appendTo(titleBar)

  private val content =
    $("<div/>").
      addClass(htmlClass("content")).
      appendTo(win)

  private val header =
    $("<div/>").
      addClass(htmlClass("win-header")).
      appendTo(content)

  private val searchDiv =
    $("<div/>").
      addClass(htmlClass("search")).
      appendTo(header)

  private val searchLine =
    $("<input/>").
      addClass(htmlClass("search-line")).
      attr("type", "text").
      appendTo(searchDiv)

  private val searchButton =
    $("<button/>").
      addClass(htmlClass("search-button")).
      attr("type", "button").
      html("Search").
      click { (_: JQueryEventObject) =>
        searchCallback(searchLine.value.toString.trim)
      }.
      appendTo(searchDiv)

  searchLine.keypress { (e: JQueryEventObject) =>
    e.which match {
      case 13 => // Enter
        searchButton.click()
      case _ =>
    }
  }

  private val queryWord =
    $("<a/>").
      addClass(htmlClass("query-word")).
      attr("target", "_blank").
      attr("title", "查看详细").
      appendTo(header)

  private val phonetic =
    $("<span/>").
      addClass(htmlClass("phonetic")).
      appendTo(header)

  private val phoneticUSAudio = {
    val a = document.createElement("audio").asInstanceOf[HTMLAudioElement]
    a.id = htmlID("phonetic-us-audio")
    a
  }

  private val phoneticUS =
    $("<a/>").
      addClass(htmlClass("phonetic-us")).
      click { (_: JQueryEventObject) =>
        phoneticUSAudio.play()
      }.
      appendTo(phonetic)

  private val phoneticUKAudio = {
    val a = document.createElement("audio").asInstanceOf[HTMLAudioElement]
    a.id = htmlID("phonetic-uk-audio")
    a
  }

  private val phoneticUK =
    $("<a/>").
      addClass(htmlClass("phonetic-uk")).
      click { (_: JQueryEventObject) =>
        phoneticUKAudio.play()
      }.
      appendTo(phonetic)

  $("<hr/>").appendTo(content)

  private val body =
    $("<div/>").
      addClass(htmlClass("body")).
      appendTo(content)

  private val loading =
    $("<p/>").
      addClass(htmlClass("loading")).
      appendTo(body)

  private val explainsList =
    $("<ul>").
      addClass(htmlClass("expains-list")).
      appendTo(body)

  $("<hr/>").appendTo(content)

  private val footer =
    $("<div/>").
      addClass(htmlClass("win-footer")).
      appendTo(content)

  private val imageSearchLink =
    $("<a/>").
      addClass(htmlClass("image-search")).
      attr("target", "_blank").
      html("Pictures").
      appendTo(footer)

  initEvent()
  initCSS()

  private def initEvent(): Unit = {
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

  private def initCSS(): Unit = {
    val backgroubndColor = "lightgray"
    val textColor = "rgb(40, 56, 70)"
    val phoneticColor = "rgb(30, 80, 150)"
    val phoneticHoverColor = "rgb(30, 100, 170)"

    val defaultWordTypeColor = "darkblue"
    val wordTypeColors = Map(
      "n" -> "red",
      "adv" -> "blue",
      "adj" -> "green"
    ).withDefaultValue(defaultWordTypeColor)

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
          |  zIndex; 99999;
          |}
          |
          |.${titleBar.attr("class")} {
          |  background: darkgray;
          |  padding: 5px 0;
          |  margin: 0;
          |  height: 25px;
          |  text-align: center;
          |}
          |
          |.${title.attr("class")} {
          |  font-size: 15px;
          |  color: ghostwhite;
          |  cursor: inherit;
          |  margin: 0;
          |}
          |
          |.${content.attr("class")} {
          |  padding-top: 10px;
          |  padding-bottom: 5px;
          |  padding-left: 5px;
          |  padding-right: 5px;
          |}
          |
          |.${searchDiv.attr("class")} {
          |  text-align: center;
          |  margin: 0 10px;
          |}
          |
          |.${searchLine.attr("class")} {
          |  font-size: 18px;
          |  padding: 1px 2px;
          |  height: 20px;
          |  width: 130px;
          |}
          |
          |.youdao-search-button {
          |	 -moz-box-shadow: 0px 1px 3px 0px #91b8b3;
          |	 -webkit-box-shadow: 0px 1px 3px 0px #91b8b3;
          |	 box-shadow: 0px 1px 3px 0px #91b8b3;
          |  background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #768d87), color-stop(1, #6c7c7c));
          |  background:-moz-linear-gradient(top, #768d87 5%, #6c7c7c 100%);
          |  background:-webkit-linear-gradient(top, #768d87 5%, #6c7c7c 100%);
          |  background:-o-linear-gradient(top, #768d87 5%, #6c7c7c 100%);
          |  background:-ms-linear-gradient(top, #768d87 5%, #6c7c7c 100%);
          |  background:linear-gradient(to bottom, #768d87 5%, #6c7c7c 100%);
          |  filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#768d87', endColorstr='#6c7c7c',GradientType=0);
          |  background-color:#768d87;
          |  -moz-border-radius:5px;
          |  -webkit-border-radius:5px;
          |  border-radius:5px;
          |  border:1px solid #566963;
          |  display:inline-block;
          |  cursor:pointer;
          |  color:#ffffff;
          |  font-family:Arial;
          |  font-size:13px;
          |  font-weight:bold;
          |  padding:5px 10px;
          |  margin-left: 5px;
          |  text-decoration:none;
          |  text-shadow:0px -1px 0px #2b665e;
          |}
          |.youdao-search-button:hover {
          |	 background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #6c7c7c), color-stop(1, #768d87));
          |	 background:-moz-linear-gradient(top, #6c7c7c 5%, #768d87 100%);
          |	 background:-webkit-linear-gradient(top, #6c7c7c 5%, #768d87 100%);
          |	 background:-o-linear-gradient(top, #6c7c7c 5%, #768d87 100%);
          |	 background:-ms-linear-gradient(top, #6c7c7c 5%, #768d87 100%);
          |	 background:linear-gradient(to bottom, #6c7c7c 5%, #768d87 100%);
          |	 filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#6c7c7c', endColorstr='#768d87',GradientType=0);
          |	 background-color:#6c7c7c;
          |}
          |.youdao-search-button:active {
          |  position:relative;
          |  top:1px;
          |}
          |
          |.${queryWord.attr("class")} {
          |  display: block;
          |  color: $textColor;
          |  text-align: center;
          |  padding: 3px;
          |  font-size: 25px;
          |  transition: font-size 0.5s;
          |  text-decoration: none;
          |}
          |
          |.${queryWord.attr("class")}:hover {
          |  color: $textColor;
          |  font-size: 30px;
          |  text-decoration: none;
          |}
          |
          |.${phonetic.attr("class")} {
          |  display: block;
          |  text-align: center;
          |  font-size: 14px;
          |}
          |
          |.${phonetic.attr("class")} a:nth-child(2) {
          |  padding-left: 10px;
          |}
          |.${phonetic.attr("class")} a {
          |  text-decoration: none;
          |  color: $phoneticColor;
          |  cursor: pointer;
          |}
          |.${phonetic.attr("class")} a:hover {
          |  color: $phoneticHoverColor
          |}
          |
          |.${loading.attr("class")} {
          |  text-align: center;
          |}
          |
          |.${explainsList.attr("class")} {
          |  padding: 0;
          |  list-style: none;
          |}
          |
          |${cssClass("explains-item")} {
          |  margin: 2px 10px;
          |  font-size: 16px;
          |  text-align: left;
          |}
          |
          |${cssClass("explain")} {
          |  color: $textColor
          |}
          |
          |${cssClass("word-type")} {
          |  color: $defaultWordTypeColor
          |}
          |${
            (for ((t, color) <- wordTypeColors) yield {
              s"${cssClass("word-type-" + t)} { color: $color; }"
            }).mkString("\n")
          }
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
          |.${imageSearchLink.attr("class")} {
          |  font-size: 10px;
          |  color: rgb(180, 175, 175);
          |  text-decoration: none;
          |}
        """.stripMargin)
  }
}
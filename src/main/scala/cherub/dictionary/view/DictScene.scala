package cherub.dictionary.view
import cherub.dictionary.{Dictionary, QueryResponse}
import cherub.js.Implicits._
import org.scalajs.dom.raw.HTMLAudioElement
import org.scalajs.dom.{document, window}
import org.scalajs.jquery.{JQuery, JQueryEventObject, jQuery => $}

/**
  * Created by cherub on 17-5-31.
  */
class DictScene extends Scene {
  // At search button clicked, send search line value to function, default do nothing
  var searchCallback: String => Unit = { _ => () }

  def showQueryInWin(dict: Dictionary, queryResponse: QueryResponse): this.type = {
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

    // 点击翻译中的单词时直接查询
    $(cssClass("word")).click { (e: JQueryEventObject) =>
      searchCallback($(e.currentTarget).text())
    }
    this
  }

  def before_query(dict: Dictionary, word: String): this.type = {
    searchLine.value(word)
    queryWord.
      html(limitLength(word, 15)).
      attr("href", dict.wordURL(word))
    phoneticUK.html("")
    phoneticUS.html("")
    imageSearchLink.attr("href", dict.imageSearchURL(word))
    this
  }

  private var loadingBlink: Int = 0

  override def startLoad(): DictScene.this.type = {
    stopLoad() // 停止之前未停止的加载

    explainsList.html("")
    loading.html("查询中,请等待...").show()

    var flag = -1
    val changeSpeed = 0.05
    def currentOpacity = loading.css("opacity").toDouble
    loadingBlink = window.setInterval({ () =>
      loading.css("opacity", currentOpacity + changeSpeed * flag)
      if (currentOpacity >= 1) {
        flag = -1
        loading.css("opacity", 1)
      } else if (currentOpacity <= 0.1) {
        flag = 1
        loading.css("opacity", 0.1)
      }
    }, 100)
    this
  }

  override def stopLoad(): this.type = {
    loading.hide()
    window.clearInterval(loadingBlink)
    this
  }

  private def renderExplains(arr: Seq[String]): Seq[String] = {
    def limitThenRender(s: String, limitLen: Int) =
      renderWordsIn(limitLengthWithNewLine(s, limitLen)).replaceAll("\n", "<br/>")

    val limitLen = 20
    for (s <- arr) yield {
      val a = s.split(" ", 2)
      if (a.length == 2 && a(0).endsWith("."))
        s"""
           <span class="${htmlClass("word-type")} ${htmlClass("word-type-" + a(0).stripSuffix("."))}">${a(0)}</span>
           <span class="${htmlClass("explain")}">${limitThenRender(a(1), limitLen)}</span>
          """
      else
        limitThenRender(a(0), limitLen)
    }
  }

  private def renderWordsIn(s: String): String = {
    val wordRe = "\\w[\\w\\s]+".r
    wordRe.replaceAllIn(s, { m =>
      $("<a/>").
        addClass(htmlClass("word")).
        html(m.matched)(0).outerHTML
    })
  }

  private def limitLength(s: String, len: Int): String = {
    if (s.length > len)
      s.slice(0, len-3) + "..."
    else
      s
  }

  private def limitLengthWithNewLine(s: String, len: Int): String = {
    if (s.length > len) {
      val (a, b) = s.splitAt(len)
      s"$a\n${limitLengthWithNewLine(b, len)}"
    } else s
  }

  override val content: JQuery =
    $("<div/>").
      addClass(htmlClass("scene"))

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
  }

  private def initCSS(): Unit = {
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
          |${cssClass("search-button")} {
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
          |${cssClass("search-button")}:hover {
          |	 background:-webkit-gradient(linear, left top, left bottom, color-stop(0.05, #6c7c7c), color-stop(1, #768d87));
          |	 background:-moz-linear-gradient(top, #6c7c7c 5%, #768d87 100%);
          |	 background:-webkit-linear-gradient(top, #6c7c7c 5%, #768d87 100%);
          |	 background:-o-linear-gradient(top, #6c7c7c 5%, #768d87 100%);
          |	 background:-ms-linear-gradient(top, #6c7c7c 5%, #768d87 100%);
          |	 background:linear-gradient(to bottom, #6c7c7c 5%, #768d87 100%);
          |	 filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#6c7c7c', endColorstr='#768d87',GradientType=0);
          |	 background-color:#6c7c7c;
          |}
          |${cssClass("search-button")}:active {
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
          |${cssClass("word")} {
          |  cursor: pointer;
          |  color: #07A;
          |}
          |${cssClass("word")}:hover {
          |  text-decoration: underline;
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

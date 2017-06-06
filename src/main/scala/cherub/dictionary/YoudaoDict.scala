package cherub.dictionary

import cherub.js.greasemonkey.GM.GM_xmlhttpRequest
import cherub.js.greasemonkey.GMXMLHttpResponse
import org.scalajs.jquery.jQuery

import scala.scalajs.js

/**
  * Created by cherub on 17-4-1.
  */
object YoudaoDict extends Dictionary {
  override def query(word: String)(callback: (Option[QueryResponse]) => Unit): Unit = {
    GM_xmlhttpRequest(js.Dynamic.
      literal(
        url = getQueryURL(word),
        method = "GET",
        headers = js.Dynamic.literal("Accept" -> "application/json"),
        onload = { (res: GMXMLHttpResponse) =>
          callback(makeQueryResponse(res))
        },
        onerror = { (res: GMXMLHttpResponse) =>
          callback(None)
        }
      ))
  }

  override def imageSearchURL(word: String): YoudaoDict.URL = s"https://www.google.com.hk/search?q=$word&tbm=isch"

  override def wordURL(word: String): YoudaoDict.URL = s"http://dict.youdao.com/search?q=$word"

  override def ukPhoneticURL(word: String): YoudaoDict.URL = s"https://dict.youdao.com/dictvoice?type=1&audio=$word"
  override def usPhoneticURL(word: String): YoudaoDict.URL = s"https://dict.youdao.com/dictvoice?type=2&audio=$word"

  def makeQueryResponse(response: GMXMLHttpResponse): Option[QueryResponse] = {
    if (response.status == 200 && response.responseText != "no query") {
      val res = jQuery.parseJSON(response.responseText)
      val explains =
        if (js.isUndefined(res.basic))
          res.translation.asInstanceOf[js.Array[String]]
        else
          res.basic.explains.asInstanceOf[js.Array[String]]
      Some(QueryResponse(
        queryWord = res.query.asInstanceOf[String],
        explains = explains,
        phonetic = Phonetic(
          us = if (js.isUndefined(res.basic)) None else res.basic.`us-phonetic`.asInstanceOf[js.UndefOr[String]].toOption,
          uk = if (js.isUndefined(res.basic)) None else res.basic.phonetic.asInstanceOf[js.UndefOr[String]].toOption)
      ))
    } else None
  }

  def getQueryURL(word: String): String =
    s"http://fanyi.youdao.com/openapi.do?type=data&doctype=json&version=1.1&relatedUrl=http%3A%2F%2Ffanyi.youdao.com%2F%23&keyfrom=fanyiweb&key=null&translate=on&q=$word&ts=${System.currentTimeMillis}"
}

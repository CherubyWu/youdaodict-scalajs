package cherub.js.greasemonkey

import org.scalajs.dom.raw.Document

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope

/**
  * Created by cherub on 17-4-1.
  */
@JSGlobalScope
@js.native
object GM extends js.Object {
  def GM_xmlhttpRequest(value: js.Dynamic): js.Dynamic = js.native
}

@js.native
trait GMXMLHttpResponse extends js.Object {
  // Properties based on a standard XMLHttpRequest object:
  def status: Int = js.native
  def statusText: String = js.native
  def readyState: Int = js.native
  def responseText: String = js.native
  def response: js.Any = js.native
  def responseHeaders: js.Any = js.native
  def responseXML: Document = js.native

  // Greasemonkey custom properties:
  def context: js.Any = js.native
  val finalUrl: String = js.native

  // Properties for progress callbacks, based on nsIDOMProgressEvent
  def lengthComputable: js.Any = js.native
  def loaded: js.Any = js.native
  def total: js.Any = js.native
}
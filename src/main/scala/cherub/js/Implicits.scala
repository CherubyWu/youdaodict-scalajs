package cherub.js

import org.scalajs.jquery.JQuery

import scala.language.implicitConversions
import scala.scalajs.js

/**
  * Created by cherub on 17-5-31.
  */
object Implicits {
  implicit class RichJQuery(jq: JQuery) {
    def moveTo(x: Double, y: Double, unit: String = "px"): JQuery = {
      jq.css(js.Dynamic.literal(
        left = s"$x$unit",
        top  = s"$y$unit"
      ))
    }
  }
}


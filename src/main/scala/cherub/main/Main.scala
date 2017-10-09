package cherub.main

import cherub.dictionary._
import cherub.dictionary.view.{DictScene, Scene, Window}
import org.scalajs.dom.raw.KeyboardEvent
import org.scalajs.dom.{MouseEvent, document}
import org.scalajs.jquery.{jQuery => $}

import scala.scalajs.js.JSApp

/**
  * Created by cherub on 17-4-1.
  */
object Main extends JSApp {
  val dict: Dictionary = YoudaoDict
  val win = new Window
  val dictScene: DictScene = new DictScene(dict)
  win.replaceScene(dictScene)


  var enable = true

  def main(): Unit = $ { () =>
    initEvent()
  }

  def initEvent(): Unit = {
    $("body").mouseup { e =>
      if (enable)
        translate(e.asInstanceOf[MouseEvent])
    }

    $("body").keydown { (e: KeyboardEvent) =>
      e.keyCode match {
        case 27 => //ESC
          if (e.altKey)
            enable = !enable
          else
            win.toggle()
        case _ => { /* Nothing */ }
      }
    }
  }

  def translate(e: MouseEvent): Unit = {
    if (win.pointInWin(e.pageX, e.pageY))
      return
    win.hideOldWin()
    dictScene.stopLoad()
    Option(document.getSelection).foreach { selection =>
      if (selection.anchorNode != null && selection.anchorNode.nodeType == 3) {
        val word = selection.toString
          .trim
          .replace("-\n", "")
          .replace("\n", " ")
        if (!word.isEmpty) {
          win.showWinAt(e.pageX, e.pageY, () => dictScene.showWord(word))
        }
      }
    }
  }
}


package scaxerciser

import com.novus.salat._
import play.api._
import play.api.Play.current

package object context {
  implicit val ctx = {
    val c = new Context() {
      val name = "Scaxerciser-1.0_context"
    }
    c.registerClassLoader(Play.classloader)
    c
  }
}
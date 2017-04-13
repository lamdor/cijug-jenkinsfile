package hello

import org.http4s._, org.http4s.dsl._
import org.http4s.server.ServerApp
import org.http4s.server.blaze._

object Hello extends ServerApp {

  val sayHello = HttpService {
    case req =>
      Ok(s"Hello CIJUG from version ${BuildInfo.version}")
  }

  override def server(args: List[String]) = {
    BlazeBuilder
      .bindHttp(8080, "0.0.0.0")
      .mountService(sayHello)
      .start
  }
}

package hello

import org.scalatest._
import org.http4s._, org.http4s.dsl._
import fs2.Task

class HelloSpec extends FlatSpec {
  it should "say hello" in {
    val getRoot = Request(Method.GET, uri("/"))
    val Some(resp) = Hello.sayHello.run(getRoot).unsafeRun.toOption
    val body = resp.as[String].unsafeRun
    assert(body == s"Hello from version ${BuildInfo.version}")
  }
}

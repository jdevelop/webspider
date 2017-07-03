package com.webspider.demo

import java.util.concurrent.ConcurrentHashMap
import java.util.function

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.webspider.agent.shared.CLIHelper
import org.apache.commons.io.IOUtils
import org.rogach.scallop.ScallopConf

object Boot {

  val ExtRegex = ".*\\.(\\w+)$".r

  val BasicUrlvalidation = "^https?://".r

  val Cache = new ConcurrentHashMap[String, Array[Byte]](10)

  def main(args: Array[String]): Unit = {

    object Conf extends ScallopConf(args) with CLIHelper.ErrorReporter {

      val restPort = opt[Int]("rest-port", default = Some(8080))
      val restHost = opt[String]("rest-host", default = Some("localhost"))

    }

    Conf.verify()

    // accept POST request with the initial URL
    val rte = (post & path("validate") & formField("url")) {
      case url if BasicUrlvalidation.findFirstMatchIn(url).isDefined ⇒
        // start processing of the data
        // send status back to the page
        complete(url)
      case url ⇒
        // start processing of the data
        // send status back to the page
        complete((400, s"'${url}' doesn't start with http/https"))
    } ~ (get & path("status" / JavaUUID)) {
      statusId ⇒
        complete(statusId.toString)
    } ~ get {
      ctx ⇒
        val path = ctx.request.uri.path.toString match {
          case "/" ⇒ "/html/index.html"
          case x ⇒ s"/html$x"
        }
        Option(getClass.getResourceAsStream(path)).fold(ctx.complete((404, "Not found"))) {
          stream ⇒
            val ct = path match {
              case ExtRegex("html") ⇒ ContentTypes.`text/html(UTF-8)`
              case ExtRegex("css") ⇒ ContentType(MediaTypes.`text/css`, HttpCharsets.`UTF-8`)
              case ExtRegex("js") ⇒ ContentType(MediaTypes.`application/javascript`, HttpCharsets.`UTF-8`)
            }
            val res = Cache.computeIfAbsent(path, new function.Function[String, Array[Byte]] {
              override def apply(t: String): Array[Byte] = {
                println("Missed ", path)
                IOUtils.toByteArray(getClass.getResourceAsStream(path))
              }
            })
            ctx.complete(
              HttpEntity(ct, ByteString(res))
            )
        }
    }

    // start REST service

    implicit val system = ActorSystem("HTTPState")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val bindingFuture = Http().bindAndHandle(rte, Conf.restHost(), Conf.restPort())

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
      }

    })


  }

}

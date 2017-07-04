package com.webspider.demo

import java.util.concurrent.{ConcurrentHashMap, TimeUnit}
import java.util.function

import akka.pattern._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}
import com.webspider.agent.node.HttpClientNode
import com.webspider.agent.shared.{Bootstrap, CLIHelper}
import com.webspider.scheduler.node.TaskSchedulerNode
import com.webspider.scheduler.node.TaskSchedulerNode.SeedUrl
import org.apache.commons.io.IOUtils
import org.rogach.scallop.ScallopConf

object Boot extends Bootstrap.BootstrapNode {

  val ExtRegex = ".*\\.(\\w+)$".r

  val BasicUrlvalidation = "^https?://".r

  val Cache = new ConcurrentHashMap[String, Array[Byte]](10)

  val to = Timeout(2, TimeUnit.SECONDS)

  def main(args: Array[String]): Unit = {

    object Conf extends ScallopConf(args) with CLIHelper.ErrorReporter {

      val restPort = opt[Int]("rest-port", default = Some(8080))
      val restHost = opt[String]("rest-host", default = Some("localhost"))

    }

    Conf.verify()

    val (c, cs) = configure(CLIHelper.ClusterConf("127.0.0.1", "127.0.0.1", 2551, 2551),
      List("akka.tcp://testcluster@127.0.0.1:2551"), "testcluster")

    HttpClientNode.start(c, cs)
    TaskSchedulerNode.start(c, cs)

    val schedulerRef = cs.actorSelection("/user/scheduler_node")

    // accept POST request with the initial URL
    val rte = (post & path("validate") & formField("url")) {
      case url if BasicUrlvalidation.findFirstMatchIn(url).isDefined ⇒
        // start processing of the data
        // send status back to the page
        implicit val timeout = to
        import cs.dispatcher
        complete(
          (schedulerRef ? SeedUrl(url)).map {
            case Some(uuid) ⇒ uuid.toString
            case None ⇒ "n/a"
          }
        )
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
        Option(getClass.getResource(path)).fold(ctx.complete((404, "Not found"))) {
          _ ⇒
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
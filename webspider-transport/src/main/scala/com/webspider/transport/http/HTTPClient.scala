package com.webspider.transport.http

import java.io.InputStream
import java.net.URI
import java.security.cert.X509Certificate

import com.typesafe.scalalogging.Logger
import io.mikael.urlbuilder.UrlBuilder
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.config.{RegistryBuilder, SocketConfig}
import org.apache.http.conn.socket.{ConnectionSocketFactory, PlainConnectionSocketFactory}
import org.apache.http.conn.ssl.{SSLConnectionSocketFactory, SSLContexts, TrustStrategy}
import org.apache.http.impl.client.{DefaultRedirectStrategy, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.protocol.HttpContext
import org.apache.http.{HttpHeaders, HttpRequest, HttpResponse}

/**
  * User: Eugene Dzhurinsky
  * Date: 1/5/15
  */
object HTTPClient {

  type StreamHandler = (String, HttpResponse) ⇒ InputStream

  private final val LOG = Logger(HTTPClient.getClass)

  val TIMEOUT: Int = 20 * 1000

  val socketConfig: SocketConfig = SocketConfig.custom()
    .setSoTimeout(TIMEOUT)
    .setTcpNoDelay(true)
    .build()

  val requestConfig: RequestConfig = RequestConfig.custom()
    .setSocketTimeout(TIMEOUT)
    .setConnectTimeout(TIMEOUT)
    .build()


  val sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy {
    override def isTrusted(chain: Array[X509Certificate], authType: String): Boolean = true
  }).build()

  val sslsf = new SSLConnectionSocketFactory(
    sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)

  val socketFactoryRegistry = RegistryBuilder.create[ConnectionSocketFactory]().
    register("https", sslsf).
    register("http", PlainConnectionSocketFactory.INSTANCE).build()

  val cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry)
  cm.setDefaultSocketConfig(socketConfig)
  cm.setMaxTotal(200)
  cm.setDefaultMaxPerRoute(20)

  private val CustomRedirectStrategy = new DefaultRedirectStrategy {
    override def createLocationURI(location: String): URI = {
      UrlBuilder.fromString(location).toUriWithException
    }

    override def getRedirect(request: HttpRequest, response: HttpResponse, context: HttpContext): HttpUriRequest = {
      configureUserAgent(
        super.getRedirect(request, response, context)
      )
    }
  }

  private def configureUserAgent(req: HttpUriRequest): HttpUriRequest = {
    req.getURI.getHost match {
      case "linkedin.com" | "www.linkedin.com" ⇒
        req.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36")
      case "t.co" | "buff.ly" ⇒
        req.setHeader(HttpHeaders.USER_AGENT, "Mozilla/1.0")
      case _ ⇒
        req.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
    }
    req
  }

  val client = HttpClients.custom()
    .setDefaultRequestConfig(requestConfig)
    .setConnectionManager(cm)
    .setRedirectStrategy(CustomRedirectStrategy)
    .build()

}
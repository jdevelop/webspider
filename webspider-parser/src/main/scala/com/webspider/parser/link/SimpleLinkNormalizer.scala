package com.webspider.parser.link

import com.webspider.core.Resource
import java.util.regex.Pattern
import java.net.URL

object SimpleLinkNormalizer {

  private val VALID_IPADDRESS_REGEX = """(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"""

  private val VALID_HOSTNAME_REGEX = """(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\-]*[A-Za-z0-9])"""

  private val ABSOLUTE_URL_REGEX = Pattern.compile("^\\w+://(" + VALID_HOSTNAME_REGEX + "|" + VALID_IPADDRESS_REGEX + ")(:\\d+)?/")

  private val SLASH = Pattern.compile("/")

  private val defaultSchemaPorts = Map(
    "http" -> 80,
    "https" -> 443,
    "ftp" -> 21
  )

}

/**
 * Simple implementation which does not expose good performance.
 */
class SimpleLinkNormalizer extends RelativeLinkNormalizer {

  import SimpleLinkNormalizer._

  override def normalize(current: String, relativeLink: String): String = {

    def wipeLevelUps(items: List[String], urlPart: String) =
      urlPart match {
        case "." | "" => items
        case ".." => items match {
          case Nil => Nil
          case _ :: rest => rest
        }
        case notEmptyUrl => notEmptyUrl :: items
      }

    def splitAndTransform(urlString: String) =
      SLASH.split(urlString).toList.foldLeft(List[String]())(wipeLevelUps).reverse.mkString("/")

    def formatPort(url: URL) =
      defaultSchemaPorts.get(url.getProtocol) match {
        case None => ":" + url.getPort
        case Some(_) => ""
      }

    def prefixIt(prefix: String, content: String) = content match {
      case null | "" => ""
      case _ => prefix + content
    }

    def suffixIt(suffix: String, content: String) = content match {
      case null | "" => ""
      case _ => content + suffix
    }

    def prepareUrl(url: URL)(newPath: String) = {
      url.getProtocol + "://" +
        suffixIt("@", url.getUserInfo) +
        url.getHost +
        formatPort(url) +
        "/" +
        newPath +
        prefixIt("?", url.getQuery) +
        prefixIt("#", url.getRef)
    }

    if (ABSOLUTE_URL_REGEX.matcher(relativeLink).find()) {
      val url = new URL(relativeLink)
      return prepareUrl(url)(
        splitAndTransform(url.getPath)
      )
    }
    val baseURL = current.endsWith("/") match {
      case true => current
      case _ => (current.lastIndexOf('/'), current.lastIndexOf("://")) match {
        case (slashIdx, protoIdx) => slashIdx > -1 match {
          case true if slashIdx - 2 == protoIdx => current + "/"
          case false => current.substring(0, slashIdx + 1)
        }
        case _ => throw NotALinkExcepton("Wrong link type: %1$s".format(current))
      }
    }
    val url = new URL(baseURL)

    prepareUrl(url)(
      relativeLink.startsWith("/") match {
        case true => splitAndTransform(relativeLink)
        case false => splitAndTransform(url.getPath + relativeLink)
      }
    )
  }

}
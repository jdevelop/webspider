webspider  [![Build Status](https://secure.travis-ci.org/jdevelop/webspider.png)](http://travis-ci.org/jdevelop/webspider)
=========

Open WEB spider platform, inspired by [LinkTiger](http://www.linktiger.com) and [PageFreezer](http://pagefreezer.com)

The related projects are listed [Here](http://java-source.net/open-source/crawlers/java-web-crawler)

## Description ##

Open WEB spider aimed to solve common task of downloading the entire content of WEB-site and allow on-fly post-processing of content.
Planned features are

 - extract text from HTML/PDF documents
 - process only documents, matching given patterns in names/content types
 - extract data using XPath expressions from not well-formed HTML pages or XHTML ones
 - maintain website graph (links between ancestor / successor pages)
 - process websites behind the authentication (HTTP Basic/Digest, Form-Based authentication)
 - handle failures and restart processing from point where application was aborted
 - provide extension API for document type handlers, protocol handlers
 - concurrent processing of website pages
 - minimize traffic using bzip/gzip encoding when possible, avoid donloading of same link twice or more times

Supported protocols:
 - HTTP(S)
 - FTP

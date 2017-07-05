webspider [![Build Status](https://travis-ci.org/jdevelop/webspider.svg?branch=master)](https://travis-ci.org/jdevelop/webspider)
=========

Open WEB spider platform. Uses [Akka Cluster](http://doc.akka.io/docs/akka/snapshot/java/cluster-usage.html) for distributed processing, along with [Distributed PubSub](http://doc.akka.io/docs/akka/snapshot/scala/distributed-pub-sub.html).

The **webspider-demo** module contains the simple web application that starts one task scheduler node, and couple of web processing nodes, and exposes the interface at http://localhost:8080/

### Planned features ###

 - extract text from HTML/PDF documents
 - process only documents, matching given patterns in names/content types
 - extract data using XPath expressions from not well-formed HTML pages or XHTML ones
 - maintain website graph (links between ancestor / successor pages)
 - process websites behind the authentication (HTTP Basic/Digest, Form-Based authentication)
 - handle failures and restart processing from point where application was aborted
 - provide extension API for document type handlers, protocol handlers
 - concurrent processing of website pages
 - minimize traffic using bzip/gzip encoding when possible, avoid donloading of same link twice or more times

### Supported protocols: ###
 - HTTP(S)

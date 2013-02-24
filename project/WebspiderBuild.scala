import sbt._
import sbt.Keys._

object WebspiderBuild extends Build {

  import Resolvers._
  import Dependencies._
  import BuildSettings._

  lazy val webspiderParent = Project(
    id = "webspider-parent",
    base = file("."),
    settings = Project.defaultSettings ++ buildSettings     
  ) aggregate(webspider, webspiderCore, webspiderParser, webspiderStorage, webspiderTransport)

  lazy val webspiderCore = Project(id = "webspider-core", base = file("webspider-core"), 
    settings = Project.defaultSettings ++ buildSettings)

  lazy val webspiderParser = Project(id = "webspider-parser", base = file("webspider-parser"), 
    settings = Project.defaultSettings ++ buildSettings) dependsOn(webspiderCore) 
  
  lazy val webspiderStorage = Project(id = "webspider-storage", base = file("webspider-storage"), 
    settings = Project.defaultSettings ++ 
    buildSettings ++ 
    Seq(libraryDependencies ++= Seq("com.sleepycat" % "je" % "5.0.58"))) dependsOn(webspiderCore) 

  lazy val webspiderTransport = Project(id = "webspider-transport", base = file("webspider-transport"), 
    settings = Project.defaultSettings ++ buildSettings) dependsOn(webspiderCore) 
  
  lazy val webspider = Project(id = "webspider", base = file("webspider"), 
    settings = Project.defaultSettings ++ buildSettings) dependsOn(webspiderCore, webspiderStorage, webspiderParser, webspiderTransport)
}

object Resolvers {
  lazy val repoResolvers = Seq("Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
    "Sonatype Public" at "https://oss.sonatype.org/content/groups/public",
    "Oracle Repository" at "http://download.oracle.com/maven")
}

object Dependencies {
  lazy val deps = Seq(
        "com.typesafe.akka" % "akka-actor_2.10" % "2.1.0",
        "com.typesafe.akka" % "akka-slf4j_2.10" % "2.1.0",
        "log4j" % "log4j" % "1.2.17",
        "org.apache.httpcomponents" % "httpclient" % "4.2.1",
        "com.github.scopt" % "scopt_2.10" % "2.1.0",
        "org.jsoup" % "jsoup" % "1.6.3",  
        "junit" % "junit" % "4.9" % "test",
        "org.specs2" %"specs2_2.10" % "1.14" % "test",        
        "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime")
}

object BuildSettings {
  import Resolvers._
  import Dependencies._

 lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.webspider",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.10.0",
    resolvers ++= repoResolvers,
    libraryDependencies ++= deps
  )
}

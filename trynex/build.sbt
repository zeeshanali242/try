name := "Trynex"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  ws,
  jdbc,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  filters,
  play.PlayImport.cache,
  specs2 % Test,
  evolutions,
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "com.github.briandilley.jsonrpc4j" % "jsonrpc4j" % "1.1",
  "org.postgresql" % "postgresql" % "9.3-1103-jdbc41",
  "org.bitcoinj" % "bitcoinj-core" % "0.12",
  "org.apache.commons" % "commons-email" % "1.3.3",
  "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.51",
  "org.bouncycastle" % "bcpg-jdk15on" % "1.51",
  "org.bouncycastle" % "bcprov-ext-jdk15on" % "1.51",
  "org.bouncycastle" % "bcmail-jdk15on" % "1.51",
  "org.web3j" % "core" % "3.1.0",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "org.json" % "json" % "20090211",
  "org.apache.httpcomponents" % "httpclient" % "4.3.4",
  "io.netty" % "netty-all" % "4.1.1.Final",
  "org.apache.velocity" % "velocity" % "1.7",
  "javax.mail"  % "mail" % "1.4.3"
)

libraryDependencies += "org.julienrf" %% "play-jsmessages" % "2.0.0"
libraryDependencies += "org.scalaj" % "scalaj-http_2.11" % "2.3.0"


resolvers ++= Seq(
  "Spy Repository" at "https://files.couchbase.com/maven2",
  Resolver.url("sbt-plugin-releases", new URL("https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalacOptions ++= Seq("-deprecation", "-feature")

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

scalariformSettings
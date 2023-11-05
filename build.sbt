name := """cask.api"""
organization := "com.cask"

version := "1.0-SNAPSHOT"

resolvers += Resolver.jcenterRepo

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)
swaggerDomainNameSpaces := Seq("models", "app.com.cask.models")

scalaVersion := "2.13.12"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0-RC2" % Test
libraryDependencies += "org.webjars" % "swagger-ui" % "4.11.1"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.17.2"

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.4.1"
libraryDependencies +="org.slf4j" % "slf4j-nop" % "1.7.26"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1"
libraryDependencies += "org.postgresql" % "postgresql" % "42.6.0"

// Better field mappings for Postgres/Slick
libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.21.1"
libraryDependencies += "com.github.tminglei" %% "slick-pg_circe-json" % "0.21.1"

libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.1.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0"


libraryDependencies += "joda-time" % "joda-time" % "2.12.5"

libraryDependencies += "com.sun.mail" % "javax.mail" % "1.6.2"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"


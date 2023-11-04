name := """duenna"""
organization := "com.example"

version := "1.0-SNAPSHOT"

resolvers += Resolver.jcenterRepo

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)
swaggerDomainNameSpaces := Seq("models", "app.models")

scalaVersion := "2.13.12"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0-RC2" % Test
libraryDependencies += "org.webjars" % "swagger-ui" % "4.11.1"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.17.2"

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "4.0.2"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2"

libraryDependencies += "org.postgresql" % "postgresql" % "42.6.0"

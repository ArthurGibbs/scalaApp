name := """cask"""
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

libraryDependencies ++= Seq(evolutions, jdbc)
libraryDependencies += "org.postgresql" % "postgresql" % "42.6.0"

// Better field mappings for Postgres/Slick
libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.20.4"
libraryDependencies += "com.github.tminglei" %% "slick-pg_circe-json" % "0.20.4"

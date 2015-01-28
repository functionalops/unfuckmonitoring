import sbt._
import Keys._

package metricity {
  object Parent {
    def version = "0.0.1"
    def organization = "functionalops"
    def name = "metricity"
    def scalaVersion = "2.11.5"
    def scalacOptions = Seq(
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Xfatal-warnings",
      "-Xlint",
      "-encoding",
      "utf8"
    )
  }

  object Versions {
    def scalaz    = "7.1.0"
    def parboiled = "1.1.6"
    def scalatest = "2.2.1"
  }

  object Build extends Build {
    /* default options at parent level */
    lazy val defaultSettings =
      Defaults.defaultSettings ++
        Seq(
          version       := Parent.version,
          organization  := Parent.organization,
          scalaVersion  := Parent.scalaVersion,
          scalacOptions := Parent.scalacOptions
        )

    /* aggregate/subproject spec */
    lazy val parent = Project("unfuckmonitoring",
      file("."),
      settings = defaultSettings
    ).aggregate(parser)

    lazy val parser = Project("metricity-parser",
      file("metricity-parser"),
      settings = defaultSettings)
  }
}

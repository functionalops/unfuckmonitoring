package functionalops.metricity

import scalaz._
import Scalaz._
import scalaz.effect._
import scala.io.Source

object ParserMain extends SafeApp {
  import scala.language.reflectiveCalls
  import parser._

  private val newline = "\n"
  private val separator = s"====${newline}"
  private val services: List[String] =
    "feedreader" ::
    "secretsharing-api" ::
    "search-recommender" ::
    "analytics-api" ::
    Nil

  private val envs: List[String] =
    "qa" ::
    "integ" ::
    "staging" ::
    Nil

  private val nodes: IO[List[ParserResult]] =
    (services map parseService).sequence

  override def runc = for {
    n <- nodes
    _ <- IO.putStrLn(separator)
    _ <- IO.putStrLn(n.mkString(newline + separator))
  } yield ()

  private def serviceFileName(service: String): String =
    "metricity-parser/src/main/resources/" + service + ".service"

  private def parseService(service: String): IO[ParserResult] =
    parseFile(serviceFileName _, service)

  private def parseFile(f: String => String, id: String): IO[ParserResult] = IO {
    val s = Source.fromFile(f(id))
    metricityProper.parseSource(s)
  }
}

package functionalops.metricity

import scalaz._
import Scalaz._

import scala.io.Source

import org.parboiled.scala._
import java.lang.String
import org.parboiled.errors.{ErrorUtils, ParsingException}

trait ParserFunctions extends ParserInstances {
  val metricityProper = new Parser {
    def parse(input: String): ParserResult =
      parseResult(parseRunner.run(input))

    def parseSource(s: Source): ParserResult =
      parse(s.mkString)

    private def parseResult(r: ParsingResult[MetricityAstNode]): ParserResult =
      r.result match {
        case Some(root) => root.right
        case None       =>
          ParserError.unknownError("Invalid source: " +
            ErrorUtils.printParseErrors(r)).left
      }

    private lazy val parseRunner: ReportingParseRunner[MetricityAstNode] =
      ReportingParseRunner(language)

    private def language: Rule1[MetricityAstNode] =
      rule { whiteSpace ~ service ~ whiteSpace ~ EOI }

    private def service: Rule1[ServiceNode] =
      rule {
        "service" ~ whiteSpace ~ namespaceId ~ slash ~ componentId ~ colon ~
        serviceId ~ optional(slash ~ version) ~ whiteSpace ~ "{" ~ whiteSpace ~
        (zeroOrMore(metric | collector)) ~ whiteSpace ~
        "}" ~~> ServiceNode.apply
      }

    private def metric: Rule1[MetricNode] =
      rule {
        "metric" ~ whiteSpace ~ metricScope ~ whiteSpace ~ metricId ~ whiteSpace ~ "{" ~ whiteSpace ~
        zeroOrMore(metricNamedValue) ~
        "}" ~ whiteSpace ~~> MetricNode.apply
      }

    private def collector: Rule1[CollectorNode] =
      rule {
        "collector" ~ whiteSpace ~ collectorType ~ whiteSpace ~ metricId ~ whiteSpace ~ "{" ~ whiteSpace ~
        zeroOrMore(collectorAttribute) ~
        "}" ~ whiteSpace ~~> CollectorNode.apply
      }

    private def namespaceId: Rule1[NamespaceIdentifier] =
      rule { oneOrMore(alphaNumDash) ~> IdentifierNode }

    private def componentId: Rule1[ComponentIdentifier] =
      rule { oneOrMore(alphaNumDash) ~> IdentifierNode }

    private def serviceId: Rule1[ServiceIdentifier] =
      rule { oneOrMore(alphaNumDash) ~> IdentifierNode }

    private def version: Rule1[VersionNode] =
      rule { oneOrMore(alphaNumDashDot) ~> VersionNode }

    private def metricId: Rule1[MetricIdentifier] =
      rule {
        oneOrMore(alphaNumDashDot | ":" | "/" | "+" | "|") ~> IdentifierNode
      }

    private def metricNamedValue: Rule1[MetricNamedValueNode] =
      rule {
        whiteSpace ~
        ( descriptionNamedNode
        | categoryNamedNode
        | unitNamedNode) ~
        whiteSpace
      }

    private def collectorAttribute: Rule1[CollectorNamedValueNode] =
      rule {
        whiteSpace ~
        ( collectorPathNamedNode
        | collectorIndexNamedNode
        | collectorArgumentsNamedNode ) ~
        whiteSpace
      }

    private def collectorPathNamedNode: Rule1[CollectorNamedValueNode] =
      rule {
        "path" ~ assign ~ string ~~> CollectorPathNamedNode.apply
      }

    private def collectorIndexNamedNode: Rule1[CollectorNamedValueNode] =
      rule {
        "index" ~ assign ~ index ~~> CollectorIndexNamedNode.apply
      }

    private def collectorArgumentsNamedNode: Rule1[CollectorNamedValueNode] =
      rule {
        "arguments" ~ assign ~ arguments ~~> CollectorArgumentsNamedNode.apply
      }

    private def descriptionNamedNode: Rule1[MetricNamedValueNode] =
      rule { "description" ~ assign ~ string ~~> DescriptionNamedNode.apply }

    private def categoryNamedNode: Rule1[MetricNamedValueNode] =
      rule { "category" ~ assign ~ metricCategory ~~> CategoryNamedNode.apply }

    private def unitNamedNode: Rule1[MetricNamedValueNode] =
      rule { "unit" ~ assign ~ metricUnit ~~> UnitNamedNode.apply }

    private def metricCategory: Rule1[MetricCategory] =
      rule {
        "count"     ~> (_ => MetricCategory.Count) |
        "rate"      ~> (_ => MetricCategory.Rate) |
        "gauge"     ~> (_ => MetricCategory.Gauge) |
        "counter"   ~> (_ => MetricCategory.Counter) |
        "timestamp" ~> (_ => MetricCategory.Timestamp)
      }

    private def metricScope: Rule1[MetricScope] =
      rule {
        "environment" ~> (_ => MetricScope.EnvironmentScope) |
        "datacenter"  ~> (_ => MetricScope.DatacenterScope) |
        "pod"         ~> (_ => MetricScope.PodScope) |
        "host"        ~> (_ => MetricScope.HostScope) |
        "instance"    ~> (_ => MetricScope.InstanceScope)
      }

    private def collectorType: Rule1[CollectorType] =
      rule {
        "procfs"      ~> (_ => CollectorType.procfs) |
        "command"     ~> (_ => CollectorType.command) |
        "sigar"       ~> (_ => CollectorType.sigar)
      }

    /**
      * Using the metrics20.org/spec as basis for the units here
      */
    private def metricUnit: Rule1[MetricUnit] =
      rule {
        "bit"       ~> (_ => MetricUnit.Bit) |
        "byte"      ~> (_ => MetricUnit.Byte) |
        "second"    ~> (_ => MetricUnit.Second) |
        "minute"    ~> (_ => MetricUnit.Minute) |
        "hour"      ~> (_ => MetricUnit.Hour) |
        "day"       ~> (_ => MetricUnit.Day) |
        "week"      ~> (_ => MetricUnit.Week) |
        "month"     ~> (_ => MetricUnit.Month) |
        "error"     ~> (_ => MetricUnit.Error) |
        "warning"   ~> (_ => MetricUnit.Warning) |
        "email"     ~> (_ => MetricUnit.EmailMessage) |
        "request"   ~> (_ => MetricUnit.Request) |
        "ticket"    ~> (_ => MetricUnit.Ticket) |
        "metric"    ~> (_ => MetricUnit.Metric) |
        "load"      ~> (_ => MetricUnit.Load) |
        "unknown"   ~> (_ => MetricUnit.Unknown)
      }

    private def string: Rule1[StringNode] =
      rule { "\"" ~ zeroOrMore(character) ~> StringNode ~ "\"" }

    private def index: Rule1[IndexNode] =
      rule { oneOrMore(digit) ~> Integer.parseInt ~~> IndexNode.apply }

    private def arguments: Rule1[Seq[StringNode]] =
      rule { zeroOrMore(string) }

    private def character: Rule0 =
      rule { escapedChar | normalChar }

    private def normalChar: Rule0 =
      rule { !anyOf("\"\\") ~ ANY }

    private def escapedChar: Rule0 =
      rule { "\\" ~ (anyOf("\"\\/bfnrt") | unicode) }

    private def unicode: Rule0 =
      rule { "u" ~ hexDigit ~ hexDigit ~ hexDigit ~ hexDigit }

    private def hexDigit: Rule0 =
      rule { "0" - "9" | "a" - "f" | "A" - "F" }

    private def slash: Rule0 =
      rule { "/" }

    private def colon: Rule0 =
      rule { ":" }

    private def assign: Rule0 =
      rule { whiteSpace ~ "=" ~ whiteSpace }

    private def comma: Rule0 =
      rule { zeroOrMore(";") }

    private def whiteSpace: Rule0 =
      rule { zeroOrMore(anyOf(" \n\r\t\f")) }

    private def alphaNumDash: Rule0 =
      rule { alphaNum | "-" }

    private def alphaNumDashDot: Rule0 =
      rule { alphaNumDash | "." }

    private def alphaNum: Rule0 =
      rule { alpha | digit }

    private def alpha: Rule0 =
      rule { "a" - "z" | "A" - "Z" }

    private def digit: Rule0 =
      rule { "0" - "9" }

    import scala.language.implicitConversions
    implicit override def toRule(s: String) =
      if (s.endsWith(" ")) { str(s.trim) ~ whiteSpace } else { str(s) }
  }
}

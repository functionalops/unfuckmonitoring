package functionalops.metricity

import scalaz._
import Scalaz._

/** Most of the types defined in this trait are heavily
  * influenced by the Metrics 2.0 specification found:
  * http://metrics20.org/spec/
  */
trait ParserTypes {
  sealed trait ParserError
  object ParserError {
    case class UnknownError(message: String) extends ParserError

    def unknownError(message: String): ParserError = UnknownError(message)
  }

  type ParserResult         = ParserError \/ MetricityAstNode

  type ServiceIdentifier    = IdentifierNode
  type NamespaceIdentifier  = IdentifierNode
  type ComponentIdentifier  = IdentifierNode
  type MetricIdentifier     = IdentifierNode
  type Version              = VersionNode

  sealed trait MetricUnit
  object MetricUnit {
    // data
    case object Bit extends MetricUnit
    case object Byte extends MetricUnit

    // time
    case object Second extends MetricUnit
    case object Minute extends MetricUnit
    case object Hour extends MetricUnit
    case object Day extends MetricUnit
    case object Week extends MetricUnit
    case object Month extends MetricUnit

    // general
    case object Error extends MetricUnit
    case object Warning extends MetricUnit
    case object EmailMessage extends MetricUnit
    case object Request extends MetricUnit
    case object Ticket extends MetricUnit
    case object Metric extends MetricUnit
    case object Probability extends MetricUnit
    case object Unknown extends MetricUnit

    // network
    case object Connection extends MetricUnit
    case object Event extends MetricUnit
    case object Socket extends MetricUnit
    case object Packet extends MetricUnit

    // filesystem
    case object Inode extends MetricUnit
    case object File extends MetricUnit

    // CPU
    case object Jiffy extends MetricUnit
    case object Load extends MetricUnit

    // messaging system
    case object Message extends MetricUnit
    case object Job extends MetricUnit

    // OS
    case object Process extends MetricUnit
    case object Thread extends MetricUnit
    case object Page extends MetricUnit
    //case object Interrupt extends MetricUnit
  }

  sealed trait MetricCategory
  object MetricCategory {
    /** Represents a number per time unit (e.g. second) */
    case object Rate extends MetricCategory

    /** Number per a given interval */
    case object Count extends MetricCategory

    /** Values at each point in time */
    case object Gauge extends MetricCategory

    /** A monotonic number that continues increasing until a reset event occurs */
    case object Counter extends MetricCategory

    /** Represents a UNIX timestamp where we can calculate the age at each point */
    case object Timestamp extends MetricCategory
  }

  sealed trait MetricScope
  object MetricScope {
    case object EnvironmentScope extends MetricScope
    case object DatacenterScope extends MetricScope
    case object PodScope extends MetricScope
    case object HostScope extends MetricScope
    case object InstanceScope extends MetricScope
  }

  sealed trait MetricityAstNode
  sealed trait MetricNamedValueNode extends MetricityAstNode
  case class DescriptionNamedNode(text: StringNode) extends MetricNamedValueNode
  case class CategoryNamedNode(category: MetricCategory) extends MetricNamedValueNode
  case class UnitNamedNode(unit: MetricUnit) extends MetricNamedValueNode
  case class OtherNamedNode(key: StringNode, value: StringNode) extends MetricNamedValueNode
  case class ServiceNode(
      ns: NamespaceIdentifier
    , component: ComponentIdentifier
    , id: ServiceIdentifier
    , version: Option[Version]
    , metrics: Seq[MetricNode]) extends MetricityAstNode

  case class MetricNode(
      scope: MetricScope
    , id: MetricIdentifier
    , attributes: Seq[MetricNamedValueNode]) extends MetricityAstNode
  case class IdentifierNode(text: String) extends MetricityAstNode
  case class VersionNode(text: String) extends MetricityAstNode
  case class StringNode(text: String) extends MetricityAstNode
}

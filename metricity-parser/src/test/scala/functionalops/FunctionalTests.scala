package functionalops.metricity

import org.scalatest.FlatSpec

import functionalops.metricity.parser._

class MetricityParserSpec extends FlatSpec {
  val validServiceSpec = """
service destrucrypt/secrets:api {
  metric host loadavg.1m {
    description = "load average for the last minute"
    unit = load
    category = gauge
  }
  metric host loadavg.5m {
    description = "load average for the last five minutes"
    unit = load
    category = gauge
  }
  metric host loadavg.15m {
    description = "load average for the last fifteen minutes"
    unit = load
    category = gauge
  }

  metric instance connections.http {
    description = "open HTTP connections"
    unit = metric
    category = gauge
  }

  metric datacenter secret.unretrieved.count {
    description = "count of unretrieved secrets"
    unit = metric
    category = gauge
  }

  metric pod customer.count {
    description = "customer count in pod"
    unit = metric
    category = gauge
  }
}
  """

  val invalidServiceConstruction = """
service destrucrypt:secrets-api {

}
  """

  val invalidMetricConstruction = """
service destrucrypt/secrets:api {
  metric metric.missing.scope {

  }
}
  """

  import scala.language.reflectiveCalls

  "A valid service construction with valid metrics" should "return a success value upon parsing" in {
    assert(metricityProper.parse(validServiceSpec).isRight)
  }

  "An invalid service construction" should "return a failure value upon parsing" in {
    val result = metricityProper.parse(invalidServiceConstruction)
    assert(result.isLeft)
  }

  "An invalid metric construction" should "return a failure value upon parsing" in {
    val result = metricityProper.parse(invalidMetricConstruction)
    assert(result.isLeft)
  }
}

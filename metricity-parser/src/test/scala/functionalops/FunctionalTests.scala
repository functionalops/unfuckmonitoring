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

  val invalidServiceSpec1 = """
service destrucrypt:secrets-api {

}
  """

  "A valid service file" should "parse returning a success value" in {
    import scala.language.reflectiveCalls
    assert(metricityProper.parse(validServiceSpec).isRight)
  }

  "An invalid service file" should "parse return a failure value" in {
    import scala.language.reflectiveCalls
    val result = metricityProper.parse(invalidServiceSpec1)
    assert(result.isLeft)
  }
}

package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class FailFastTest extends Simulation {

  private val baseUrl = System.getProperty("PAYMENTS_BASE_URL", "http://localhost:8081")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val feeder = csv("data/payment-data.csv").circular

  val createPayment = exec(
    http("Create Payment")
      .post("/payment-initiation/api/v1/payments/initiate")
      .header("X-Correlation-ID", "#{correlationId}")
      .header("X-Tenant-ID", "TENANT-TEST-001")
      .header("X-Business-Unit-ID", "BU-TEST-001")
      .body(ElFileBody("bodies/eft-payment.json")).asJson
      .check(status.in(200, 201))
      .check(bodyString.saveAs("responseBody"))
      .check(header("Content-Type").saveAs("contentType"))
  )

  val scn = scenario("Fail Fast Test")
    .feed(feeder)
    .exec { session =>
      session
        .set("reference", "TEST-REF-001")
        .set("correlationId", java.util.UUID.randomUUID().toString())
        .set("paymentId", java.util.UUID.randomUUID().toString())
        .set("idempotencyKey", java.util.UUID.randomUUID().toString())
    }
    .exec(createPayment)

  setUp(
    scn.inject(
      atOnceUsers(1) // Single user for quick fail-fast testing
    )
  ).protocols(httpProtocol)
    .assertions(
      global.failedRequests.percent.is(0) // Fail immediately on any error
    )
    .maxDuration(30.seconds) // Fail fast timeout
}

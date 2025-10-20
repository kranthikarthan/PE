package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class SustainedLoadTest extends Simulation {

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
  )

  val scn = scenario("Sustained EFT Payments")
    .feed(feeder)
    .exec { session =>
      session
        .set("reference", java.util.UUID.randomUUID().toString().substring(0,8))
        .set("correlationId", java.util.UUID.randomUUID().toString())
        .set("paymentId", java.util.UUID.randomUUID().toString())
        .set("idempotencyKey", java.util.UUID.randomUUID().toString())
    }
    .exec(createPayment)

  setUp(
    scn.inject(
      rampUsersPerSec(50).to(200).during(2.minutes),
      constantUsersPerSec(200).during(10.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile3.lte(3000), // p95 < 3s
      global.responseTime.percentile4.lte(5000), // p99 < 5s
      global.failedRequests.percent.lte(1.0)
    )
    .maxDuration(15.minutes) // Fail fast if test runs too long
}



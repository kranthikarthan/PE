package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class PeakLoadTest extends Simulation {

  private val baseUrl = System.getProperty("PAYMENTS_BASE_URL", "http://localhost:8081")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val feeder = csv("data/payment-data.csv").circular

  val createPayment = exec(
    http("Create Payment - Peak")
      .post("/payment-initiation/api/v1/payments")
      .body(ElFileBody("bodies/eft-payment.json")).asJson
      .check(status.in(200, 201))
  )

  val scn = scenario("Peak EFT Payments")
    .feed(feeder)
    .exec(createPayment)

  setUp(
    scn.inject(
      nothingFor(10.seconds),
      rampUsersPerSec(200).to(500).during(2.minutes),
      constantUsersPerSec(500).during(5.minutes)
    )
  ).protocols(httpProtocol)
}



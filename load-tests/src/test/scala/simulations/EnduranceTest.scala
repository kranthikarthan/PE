package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class EnduranceTest extends Simulation {

  private val baseUrl = System.getProperty("PAYMENTS_BASE_URL", "http://localhost:8081")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val feeder = csv("data/payment-data.csv").circular

  val createPayment = exec(
    http("Create Payment - Endurance")
      .post("/payment-initiation/api/v1/payments")
      .body(ElFileBody("bodies/eft-payment.json")).asJson
      .check(status.in(200, 201))
  )

  val scn = scenario("Endurance EFT Payments")
    .feed(feeder)
    .exec(createPayment)

  setUp(
    scn.inject(
      constantUsersPerSec(100).during(60.minutes)
    )
  ).protocols(httpProtocol)
}



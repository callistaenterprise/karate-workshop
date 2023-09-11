package se.callista.workshop.karate.product

import com.intuit.karate.gatling.PreDef._
import io.gatling.core.Predef._
import scala.concurrent.duration._

class ProductSimulation extends Simulation {

  val protocol = karateProtocol()
  protocol.nameResolver = (req, ctx) => req.getHeader("karate-name")
  protocol.runner.karateEnv("performance")

  val productScenario = scenario("Karate Product Scenario")
    .exec(karateFeature(
      "classpath:se/callista/workshop/karate/product/product.feature@performance")
    )

  setUp(
    productScenario.inject(rampUsers(100).during(10))
      .protocols(protocol)
  )
}

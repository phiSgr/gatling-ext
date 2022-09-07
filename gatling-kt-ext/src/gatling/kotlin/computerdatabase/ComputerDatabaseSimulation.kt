package computerdatabase

import com.github.phisgr.gatling.kt.chain
import com.github.phisgr.gatling.kt.on
import com.github.phisgr.gatling.kt.peek
import com.github.phisgr.gatling.kt.scenario
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.util.concurrent.ThreadLocalRandom

class ComputerDatabaseSimulation : Simulation() {
    init {
        val feeder = csv("search.csv").random()

        val search = chain {
            +http("Home").get("/")
            +pause(1)
            +feed(feeder)
            +http("Search")
                .get("/computers?f=#{searchCriterion}")
                .check(
                    css("a:contains('#{searchComputerName}')", "href").saveAs("computerUrl")
                )
            +pause(1)
            +http("Select")
                .get("#{computerUrl}")
                .check(status().shouldBe(200))
            +pause(1)
        }

        val browse = repeat(4, "i").on {
            +http("Page #{i}").get("/computers?p=#{i}")
            +pause(1)
        }

        val edit = chain {
            +tryMax(2).on {
                +http("Form")
                    .get("/computers/new")
                +pause(1)
                +http("Post")
                    .post("/computers")
                    .formParam("name", "Beautiful Computer")
                    .formParam("introduced", "2012-05-30")
                    .formParam("discontinued", "")
                    .formParam("company", "37")
                    .check(
                        status().shouldBe {
                            // we do a check on a condition that's been customized with
                            // a lambda. It will be evaluated every time a user executes
                            // the request
                            200 + ThreadLocalRandom.current().nextInt(2)
                        }
                    )
            }
            +exitHereIfFailed()
        }

        val httpProtocol =
            http.baseUrl("https://computer-database.gatling.io")
                .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .acceptLanguageHeader("en-US,en;q=0.5")
                .acceptEncodingHeader("gzip, deflate")
                .userAgentHeader(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0"
                )

        val users = scenario("Users") {
            +search
            +browse
        }
        val admins = scenario("Admins") {
            +search
            +browse
            +edit
            +peek {
                println(it)
            }

        }

        setUp(
            users.injectOpen(rampUsers(10).during(10)),
            admins.injectOpen(rampUsers(2).during(10))
        ).protocols(httpProtocol)
    }
}

package computerdatabase

import com.github.phisgr.gatling.generic.SessionCombiner
import com.github.phisgr.gatling.kt.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import java.util.*

private val timer = Timer()
fun delay(time: Long, task: () -> Unit) {
    timer.schedule(object : TimerTask() {
        override fun run() {
            task()
        }
    }, time)
}

class IllegalArgumentNoStackTrace(message: String?) : IllegalArgumentException(message) {
    override fun fillInStackTrace(): Throwable = this
}

class GenericSimulation : Simulation() {
    init {
        val errorMessageAttribute = "errorMessage"

        val s = scenario("Generic") {
            +hook { it.set("start", System.currentTimeMillis()) }
            +repeat(10, "i").on {
                +async(
                    "sleeper#{i}",
                    blockingAction("Sleep") { Thread.sleep(1000) }
                )
            }
            +repeat(10, "i").on {
                +await("sleeper#{i}", SessionCombiner.NoOp())
            }
            +peek { session ->
                val now = System.currentTimeMillis()
                println("Now is $now. Done in ${now - session.getLong("start")} ms.")
            }
            +callbackAction<Unit>("Function Fail") { _, _ ->
                throw IllegalArgumentNoStackTrace("A request is not made.")
            }
            +async(
                "errorAction",
                callbackAction("Expected Fail") { _, callback ->
                    delay(1000) {
                        callback(Result.failure(IllegalArgumentException("Fail!")))
                    }
                }.check(errorMessage.saveAs(errorMessageAttribute))
            )
            +callbackAction("Unexpected Fail") { _, callback ->
                callback(Result.failure(IllegalArgumentException("Fail!")))
            }
            +doIfOrElse { it.userId() % 2 == 1L }.then {
                +await("errorAction", SessionCombiner.pick(errorMessageAttribute))
            }.orElse {
                +await("errorAction") { _, _ ->
                    throw IllegalArgumentNoStackTrace("I want moar code coverage!")
                }
            }
            +peek { session ->
                println("User ID is ${session.userId()}. Error message is '${session.getString(errorMessageAttribute)}'.")
            }
            +callbackAction("Expected Failure, Succeeded") { _, callback ->
                callback(Result.success("silent"))
            }.silent()
                .checkIf { session -> session.userId() == 1L }
                .then(errorMessage.notExists())
            +coroutineAction("Check") { listOf(1, 2, 3) }
                .check(
                    { extractMultiple { it.get() }.find(2).shouldBe(3) },
                    { extractMultiple { it.get() }.find(0) },
                    { extractMultiple { it.get() }.find(3).notExists() },
                    { extractMultiple { it.get() }.count().shouldBe(3) },
                    { extractMultiple { it.get() }.findAll().shouldBe(listOf(1, 2, 3)) }
                )
            +blockingAction("CheckIf") { session ->
                if (session.userId() < 2L) throw RuntimeException("error message goes here")
            }.checkIf { result, _ -> result.isFailure }
                .then(errorMessage.notExists())
        }

        setUp(
            s.injectOpen(atOnceUsers(5))
        )
    }
}

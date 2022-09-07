# Gatling-KT-Ext

Unholy Kotlin extensions to the Gatling Java API.

[Async-await](../README.md#async-await) and
[generic code execution](../README.md#call-any-javascala-code)
in your Java/Kotlin Gatling tests.

Most importantly, mutable builder syntax if you write your tests in Kotlin.

## Mutable Builder Syntax

Instead of using Gatling's method chaining,
you can use the mutable builder syntax, which is idiomatic in Kotlin,
to write your tests.

```java
ChainBuilder search =
    exec(http("Home").get("/"))
        .pause(1)
        .feed(feeder)
        .exec(
            http("Search")
                .get("/computers?f=#{searchCriterion}")
                .check(
                    css("a:contains('#{searchComputerName}')", "href").saveAs("computerUrl")
                )
        )
        ...
```

[Source](https://github.com/gatling/gatling/blob/main/gatling-bundle-samples/src/main/java/computerdatabase/ComputerDatabaseSimulation.java)

```kotlin
import com.github.phisgr.gatling.kt.*

val search = chain {
    +http("Home").get("/")
    +pause(1)
    +feed(feeder)
    +http("Search")
        .get("/computers?f=#{searchCriterion}")
        .check(
            css("a:contains('#{searchComputerName}')", "href").saveAs("computerUrl")
        )
    ...
}
```

[Source](src/gatling/kotlin/computerdatabase/ComputerDatabaseSimulation.kt)

Note how the two `http` calls are in the same indentation level In the Kotlin code. 

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"

    id("io.gatling.gradle") version "3.8.3.2"

    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("com.github.phisgr:gatling-ext:0.4.0")
    api("io.gatling:gatling-core-java:3.8.3")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    gatlingImplementation("com.github.phisgr:gatling-ext:0.4.0")
    gatlingImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xlambdas=indy"
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

val javadocJar = tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaHtml"))
}

publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = properties["ossrhUsername"] as String?
                password = properties["ossrhPassword"] as String?
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.phisgr"
            artifactId = "gatling-kt-ext"
            version = "0.4.0"

            from(components["java"])

            pom {
                name.set("gatling-kt-ext")
                description.set("Unholy Kotlin extensions to the Gatling Java API")
                url.set("https://github.com/phiSgr/gatling-ext/gatling-kt-ext")

                licenses {
                    license {
                        name.set("APL2")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("phiSgr")
                        name.set("George Leung")
                        email.set("phisgr@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/phiSgr/gatling-ext/gatling-kt-ext")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

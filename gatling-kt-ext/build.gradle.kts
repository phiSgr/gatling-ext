import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"

    id("io.gatling.gradle") version "3.8.3.2"
    id("maven-publish")
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
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.phisgr"
            artifactId = "gatling-kt-ext"
            version = "0.4.0"

            from(components["java"])
        }
    }
}

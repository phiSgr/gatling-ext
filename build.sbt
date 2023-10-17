// the enterprisePackage task is confused
ThisBuild / Gatling / publishArtifact := false
ThisBuild / GatlingIt / publishArtifact := false

val commonSettings = Seq(
  organization := "com.github.phisgr",
  scalaVersion := "2.13.12",
  crossPaths := false,
)

val gatlingVersion = "3.9.5"
val gatlingCore = "io.gatling" % "gatling-core" % gatlingVersion

val publishSettings = {
  import xerial.sbt.Sonatype.*
  Seq(
    publishTo := SonatypeKeys.sonatypePublishTo.value,
    publishMavenStyle := true,

    licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    sonatypeProjectHosting := Some(GitHubHosting("phiSgr", "gatling-ext", "phisgr@gmail.com")),
  )
}


lazy val root = (project in file("."))
  .enablePlugins(GatlingPlugin)
  .settings(commonSettings *)
  .settings(publishSettings *)
  .settings(
    name := "gatling-ext",
    version := "0.5.0",
    scalacOptions ++= Seq(
      "-language:existentials",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-Xlint",
      "-opt:l:method",
    ),
    libraryDependencies ++= Seq(
      gatlingCore,
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test",
      "io.gatling" % "gatling-test-framework" % gatlingVersion % "test",
    ),
  )

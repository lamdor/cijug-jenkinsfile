organization := "io.rubbish"
name := "devopsdsm-hello"

scalaVersion in ThisBuild := "2.11.8"
scalaOrganization in ThisBuild := "org.typelevel"

val http4sVersion = "0.16.0-cats-SNAPSHOT"
val circeVersion = "0.7.0"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.9.0",
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "ch.qos.logback" %  "logback-classic" % "1.2.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version)
buildInfoPackage := "hello"

enablePlugins(DockerPlugin)
dockerAutoPackageJavaApplication("openjdk:8-jre-alpine")
imageNames in docker := Seq(
  ImageName(s"rubbish/${name.value}:${version.value}"),
  ImageName(s"rubbish/${name.value}:latest")
)

// release process
import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  inquireVersions,                        // : ReleaseStep
  setReleaseVersion,                      // : ReleaseStep
  commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
  tagRelease,                             // : ReleaseStep
  releaseStepTask(sbtdocker.DockerKeys.dockerBuildAndPush), // : ReleaseStep
  setNextVersion,                         // : ReleaseStep
  commitNextVersion,                      // : ReleaseStep
  pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
)

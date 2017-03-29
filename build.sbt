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
// set next release version
import sbtrelease._
import ReleaseTransformations._

releaseVersion := { _ =>
  val lastGitTagVersion = {
    for {
      git   <- releaseVcs.value
      allTags = git.cmd("tag", "-l").lines.toList
      lastTag <- allTags.flatMap { str =>
        Version.apply(str.replaceAll("^v", ""))
      }.sortBy(v => (- v.major, - v.subversions.head)).headOption
    } yield lastTag
  }
  lastGitTagVersion.map(_.bumpMinor.toString).getOrElse("0.1")
}

releaseProcess := Seq[ReleaseStep](
  inquireVersions,
  setReleaseVersion,
  // update deployment yaml to version
  commitReleaseVersion.copy(check = identity),
  tagRelease,
  releaseStepTask(sbtdocker.DockerKeys.dockerBuildAndPush)
)

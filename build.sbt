organization := "io.rubbish"
name := "cijug-hello"

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
  lastGitTagVersion.map(_.bumpMinor.string).getOrElse("0.1")
}

releaseProcess := Seq[ReleaseStep](
  inquireVersions,
  setReleaseVersion,
  commitReleaseVersion.copy(check = identity),
  releaseStepTask(updateK8sImageInDeploymentYaml),
  releaseStepTask(gitCommitK8sDeploymentYaml),
  tagRelease,
  releaseStepTask(releasePushTags),
  releaseStepTask(sbtdocker.DockerKeys.dockerBuildAndPush)
)


val k8sDeploymentYamlFile = settingKey[File]("k8s deployment file")
k8sDeploymentYamlFile := baseDirectory.value / "k8s/cijug-hello.yaml"

val updateK8sImageInDeploymentYaml = taskKey[Unit]("Updates the k8s/cijug-hello.yaml deployment image")
updateK8sImageInDeploymentYaml := {
  val yamlLines = IO.readLines(k8sDeploymentYamlFile.value)
  val imageRegex = s"image: rubbish/${name.value}.+"
  val updatedLines = yamlLines.map(_.replaceAll(imageRegex, s"image: rubbish/${name.value}:${version.value}"))
  IO.writeLines(k8sDeploymentYamlFile.value, updatedLines)
}

val gitCommitK8sDeploymentYaml = taskKey[Unit]("Commits k8s/cijug-hello.yaml changes")
gitCommitK8sDeploymentYaml := {
  releaseVcs.value.map { git =>
    (git.add(k8sDeploymentYamlFile.value.getPath) !)
    (git.commit(s"Update k8s deployment image to ${version.value}", false) !)
  }
}

val releasePushTags = taskKey[Unit]("push tags")
releasePushTags :=
  releaseVcs.value.map { git => (git.cmd("push", "--tags", "origin") !) }

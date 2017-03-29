#!/usr/bin/env groovy

stage("Test") {
  node {
    def sbtHome = tool("sbt 0.13.13")

    ansiColor {
      try {
        sh "${sbtHome}/bin/sbt test"
      } finally {
        junit(testResults: "target/test-reports/*.xml", allowEmptyResults: true)
      }
    }
  }
}


if (env.BRANCH_NAME == "master") {
  stage("Release") {
    node {
      def sbtHome = tool("sbt 0.13.13")

      ansiColor {
        sh "${sbtHome}/bin/sbt release"
      }
    }
  }
}

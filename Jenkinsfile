#!/usr/bin/env groovy

stage("Test") {
  node {
    checkout scm

    try {
      def sbtHome = tool("sbt 0.13.13")
      ansiColor {
        sh "${sbtHome}/bin/sbt test"
      }
    } finally {
      junit(testResults: "target/test-reports/*.xml", allowEmptyResults: true)
    }
  }
}


if (env.BRANCH_NAME == "completed") {
  stage("Release") {
    node {
      checkout scm

      def sbtHome = tool("sbt 0.13.13")
      ansiColor {
        sh '''
        git config --global user.email "jenkins@rubbish.io"
        git config --global user.name "Jenkins"
'''
        sh "${sbtHome}/bin/sbt \"release with-defaults\""
      }
    }
  }
}

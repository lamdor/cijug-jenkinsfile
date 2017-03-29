#!/usr/bin/env groovy

releasedVersion = ""

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
        sh '''
        git config --global user.email "jenkins@rubbish.io"
        git config --global user.name "Jenkins"
        git config --global credential.helper cache
'''
      checkout scm

      def sbtHome = tool("sbt 0.13.13")
      ansiColor {
        sh "${sbtHome}/bin/sbt \"release with-defaults\""

        releasedVersion = sh(script: "git describe --abbrev=0 --tags", returnStdout: true).trim().replaceAll("^v", "")
      }
    }
  }

  stage("Staging deploy") {
    node {
      checkout scm
      sh "git checkout v${releasedVersion}"

      def kubctlHome = tool("kubectl")
      sh "kubectl apply -f k8s/ --namespace staging"
    }
  }

  input 'Deploy to production?'

  stage("Production deploy") {
    node {
      checkout scm
      sh "git checkout v${releasedVersion}"

      def kubctlHome = tool("kubectl")
      sh "kubectl apply -f k8s/ --namespace staging"
    }
  }
}

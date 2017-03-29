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

stage("Test") {
  node {
    checkout scm

    try {
      def sbtHome = tool("sbt 0.13.15")
      ansiColor {
        sh "${sbtHome}/bin/sbt test"
      }
    } finally {
      junit(testResults: "target/test-reports/*.xml", allowEmptyResults: true)
    }
  }
}

releasedVersion = ""

if (env.BRANCH_NAME == "master") {
  stage("Release") {
    node {
      withCredentials([file(credentialsId: 'github-id-rsa', variable: 'id_rsa')]) {

        sh '''
          git config --global user.email "jenkins@rubbish.io"
          git config --global user.name "Jenkins"
          mkdir -p ~/.ssh
          mv ${id_rsa} ~/.ssh/id_rsa
          chmod 600 ~/.ssh/id_rsa
          git config --global url."git@github.com:rubbish".insteadOf https://github.com/rubbish
        '''
      }

      checkout scm

      def sbtHome = tool("sbt 0.13.15")
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

  input "Deploy ${releasedVersion} to production?"

  stage("Production deploy") {
    node {
      checkout scm
      sh "git checkout v${releasedVersion}"

      def kubctlHome = tool("kubectl")
      sh "kubectl apply -f k8s/ --namespace production"
    }
  }
}

def getPrOpen(String credsID){

      def parts = env.CHANGE_URL.split("/")
      def repo = parts[3]
      def repoName = parts[4]
      def pullRequestNumber = parts[6]
      def GitHubApiPR = "https://api.github.com/repos/${repo}/$repoName/pulls/${pullRequestNumber}"
      
      withCredentials([usernamePassword(credentialsId: credsID, usernameVariable: 'gitUser', passwordVariable: 'gitPwd')]) {
          // Your build steps here
          if (env.CHANGE_ID && env.CHANGE_TARGET) {
              echo "This build is triggered by a pull request."
              // Check if the pull request is open
              def prStatus = sh(script: "curl -s -H \"Authorization: Bearer \$gitPwd\" " +
                  "\"${GitHubApiPR}\" | jq -r .state",
                  returnStdout: true).trim()

              if (prStatus == "open") {
                    return prStatus

              } else {
                  error "The pull request is not open."
              }
          } else {
              echo "This build is not triggered by a pull request."
          }
      }
}

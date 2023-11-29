def getPrOpen(String credsID, String gitUser, String repoName){

      withCredentials([usernamePassword(credentialsId: credsID, usernameVariable: 'gitUser', passwordVariable: 'gitPwd')]) {
          // Your build steps here
          if (env.CHANGE_ID && env.CHANGE_TARGET) {
              echo "This build is triggered by a pull request."
              
              // Check if the pull request is open
              def prStatus = sh(script: "curl -s -H \"Authorization: Bearer \$gitPwd\" " +
                  "\"https://api.github.com/repos/\$gitUser/\$repoName/pulls/\$CHANGE_ID\" | jq -r .state",
                  returnStdout: true).trim()
              
              if (prStatus == "open") {
                  echo "The pull request is open."
              } else {
                  error "The pull request is not open."
              }
          } else {
              echo "This build is not triggered by a pull request."
          }
      }
}

/**
 * Example usage in jenkins:
 * gitRevert ("jenkins-caap01t@ifao.net", 'yaml-validation.yml', "${jenkins_qa_key}", "${jenkins_qa_passphrase}" )
*/

def call (String userEmail, String userName, String gitUsername, String gitPassword, String gitUrl) {
    String gitExecution="""
        git config --global user.email "${userEmail}"
        git config --global user.name "${userName}"
        git remote set-url origin https://${gitUsername}:${gitPassword}@${gitUrl}
        git revert --no-edit HEAD -m 1
        git push https://${gitUsername}:${gitPassword}@${gitUrl}
        """

    def statusCode = sh(
        script: "${gitExecution}",
        returnStatus: true,
        returnStdout: false
      )

      if (statusCode != 0) {
        echo "Stage failed with exitcode ${statusCode}"
        exit $ {
          statusCode
        }
      }

      echo "Stage accomplished with no errors"
}
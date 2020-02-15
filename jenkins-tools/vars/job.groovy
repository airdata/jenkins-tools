def discardBuilds(buildToKeep = 5, artifactToKeep = 5) {
    properties([
            [
            $class  : 'jenkins.model.BuildDiscarderProperty',
            strategy: [
                    $class: 'LogRotator',
                    numToKeepStr        : "${buildToKeep}",
                    artifactNumToKeepStr: "${artifactToKeep}"
            ]],
            disableConcurrentBuilds()
    ])
}

def devAutoDeploy(projectName, devBranch = 'dev', taskName="${projectName}-deploy") {
  if (devBranch == env.BRANCH_NAME) {
      build job: "../${projectName}-tasks/${taskName}",
              parameters: [
                      string(name: 'BUILD_ENV', value: 'dev'),
                      string(name: 'ARCHIVE_BUILD_NUMBER', value: '')
              ],
              wait: false
  }
}
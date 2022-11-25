def checkoutStage (branchName = null) {
    stage('checkout') {
      if (branchName == null) {
        checkout scm
        return
      }
    
      checkout scm: [$class: 'GitSCM', branches: [[name: "origin/${branchName}"]],  userRemoteConfigs: [[url: scm.userRemoteConfigs[0].url]]]
    }
}

def checkoutStageWithDist(sourceBranch = null) {
  checkoutStage(sourceBranch)
  artifacts.createDist()
}

def createTagStage(String tagName) {
  stage('creating tag') {
    sh "git commit --author='Jenkins <jenkins@ingimax.com>' --message 'chore: prepare version ${tagName}'"
    sh "git tag ${tagName}"
    sh "git push origin --tags ${tagName}"
  }
}

def updateDeliveryStage(String deliveryBranch, String tagName) {
  stage('reset delivery to tag') {
    sh "git branch --force ${deliveryBranch} ${tagName}"
    sh "git push origin --force ${deliveryBranch}:${deliveryBranch}"
  }
}

def pushNextVersionStage(String tagName, String sourceBranch) {
  sh "git commit --author='Jenkins <jenkins@ingimax.com>' --message 'chore: prepare next version ${tagName}'"
  sshagent (credentials: ['jenkins_ssh']) {
    sh "git push origin HEAD:${sourceBranch}"
  }
}

def getAllBranches(repoName) {
  return sh(script: "git ls-remote --heads git@bitbucket.org:ingicare/${repoName}.git | sed 's?.*refs/heads/??'", returnStdout: true).trim()
}

def discardFileChange(String fileName) {
  sh "git checkout ${fileName}"
}

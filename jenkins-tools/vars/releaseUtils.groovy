def mainReleaseInput(version) { 
  return input(
    id: 'versionUserInput', message: 'Ready to tag new version', parameters: [
      [$class: 'TextParameterDefinition', defaultValue: version, description: 'Version to release', name: 'version']
    ])
}

def userBranchInput (String repoName) {
  return input(
    id: 'userInput', message: 'release job required params', parameters: [
      choice(name: 'sourceBranch', choices: gitUtils.getAllBranches(repoName), description: 'Release source branch')
    ])
}

def mobileVersionInput (String dir) {
  def currentVersion = versionUtils.getMobileVersion(dir)

  return input(
    id: 'mobileVersionInput', message: 'Would you like to release a new mobile version', parameters: [
      [$class: 'TextParameterDefinition', defaultValue: currentVersion, description: 'Mobile version to release', name: 'mobileVersion']
    ])
}

def isMaintenanceBranch (String branch) {
  return branch =~ /(\d+)\.(\d+)\.(.*)\/maint/
}

def getBackendRelease (config) {
  return getReleaseByType(config, 'mvn')
}

def getMobileRelease (config) {
  return getReleaseByType(config, 'cordova')
}

def getReleaseByType(config, type) {
  return config.find {it -> it.type == type}
}


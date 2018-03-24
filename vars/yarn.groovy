def install(parameters = [:]) {
    def isDone = fileExists "node_modules"

    if (isDone) {
      return
    }

    sh "yarn install --mutex network"

    test(parameters)
}
    
def test(parameters = [:]) {
    if(parameters.skipTests != null && parameters.skipTests == true) return

    sh "yarn run test"
    if(parameters.resultsPattern) {
        junitArchiver.archive( parameters.resultsPattern )
    }
}

def build(env = null, options = "") {
    if(env == null) {
        sh "${runBuild()}"
        return
    }
        
    def command = "${runBuild()} --env ${env}"
    command = options ? "${command} ${options}" : command
    sh "${command}"
}

private def runBuild() {
    return "yarn run build"
}

def androidBuildRelease(String env) {
  setupPlatform(env)
  sh "yarn run android-build --release"
}

def androidBuildDebug(String env) {
  setupPlatform(env)
  sh "yarn run android-build"
}

def iosBuildRelease(String env){
  setupPlatform(env)
  sh "yarn run ios-build --release"  
}

def iosBuildDebug(String env){
  setupPlatform(env)
  sh "yarn run ios-build"
}

private def setupPlatform(String env) {
  install([skipTests: true])
  build(env)
  resetPlatforms()
}

private def resetPlatforms () {
  def isTaskDone = fileExists "plugins/fetch.json"

  if (isTaskDone) {
    return
  }

  sh "yarn run reset-platforms"  
}
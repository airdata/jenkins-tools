def install(parameters = [:]) {
    def isDone = fileExists "node_modules"

    if (isDone) {
      return
    }

    sh "npm install"

    test(parameters)
}

def test(parameters = [:]) {
    if(parameters.skipTests != null && parameters.skipTests == true) return

    sh "npm run test"
    if(parameters.resultsPattern) {
        junitArchiver.archive( parameters.resultsPattern )
    }
}

def build(env = null, options = "") {
    if(env == null) {
        sh "${runBuild()}"
        return
    }
        
    def command = "${runBuild()} -- --env ${env}"
    command = options ? "${command} ${options}" : command
    sh "${command}"
}

private def runBuild() {
    return "npm run build"
}

def androidBuildRelease(String env) {
  setupPlatform(env)
  sh "npm run android-build -- --release"
}

def androidBuildDebug(String env) {
  setupPlatform(env)
  sh "npm run android-build"
}

def iosBuildRelease(String env){
  setupPlatform(env)
  sh "npm run ios-build -- --release"  
}

def iosBuildDebug(String env){
  setupPlatform(env)
  sh "npm run ios-build"
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

  sh "npm run reset-platforms"  
}
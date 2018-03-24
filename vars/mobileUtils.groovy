def buildAppEnvs(body) {
  def params = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = params
  body()

  params.envs.each { env ->
    params.callback.call(env, params.build)
  }
}

def androidDebugMode() {
  return {env, config ->
    setAppByEnv(config.multipleAppVersions, env)  
    config.builderTool.androidBuildDebug(env)
    androidArchive(env, config.projectName, 'debug')   
  }
} 

def androidReleaseMode() {
  return {env, config ->
    setExternalPkgId(env, config.packageId)
    config.builderTool.androidBuildRelease(env)
    jarsignAndroid(config.projectName)
    androidArchive(env, config.projectName, 'release')
  }
}

def iosDebugMode() {
  return {env, config ->
    setAppByEnv(config.multipleAppVersions, env)
    config.builderTool.iosBuildDebug(env)
    iosArchive(env, config.projectName, 'debug')
  }
} 

def iosReleaseMode() {
  return {env, config ->
    setExternalPkgId(env, config.packageId)
    config.builderTool.iosBuildRelease(env)
    iosArchive(env, config.projectName, 'release')
  }
}

def setAppByEnv (enabled, env) {
  if (enabled) {
    gitUtils.discardFileChange('config.xml')

    def currentPkgId = getCurrentPkgId()
    def currentAppName = getCurrentAppName()

    sh "snow package ${currentPkgId}.${env}"
    sh "snow name '${currentAppName} ${env}'"
  }
}

def jarsignAndroid(projectName) {
  sh "cp ${Constant.APK_RELEASE_PATH}/android-release-unsigned.apk ${Constant.APK_RELEASE_PATH}/android-release-signed.apk"
  withCredentials([[$class: 'FileBinding', credentialsId: 'keystore', variable: 'FILE'],
                  [$class: 'StringBinding', credentialsId: 'keystorepass', variable: 'PASS'],
                  [$class: 'StringBinding', credentialsId: 'keystorealias', variable: 'ALIAS']]) {
    sh "jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass ${PASS} -keystore ${FILE} ${Constant.APK_RELEASE_PATH}/android-release-signed.apk ${ALIAS}"
  }
}

def zipalignAndroid(projectName, env, version) {
  def workspace = pwd()
  sh "/opt/android/build-tools/26.0.2/zipalign -v 4 ${workspace}/${Constant.APK_RELEASE_PATH}/android-release-signed.apk ${WORKSPACE}/${Constant.ARCHIVE_DIRECTORY}/${projectName}-${env}-${version}-signed.apk"
}

def androidArchive (env, projectName, mode) {
  def version = versionUtils.getFrontendVersion()
  
  if(mode.equals('release')) {
    zipalignAndroid(projectName, env, version)
    sh "cp ${Constant.APK_RELEASE_PATH}/android-release-unsigned.apk ${WORKSPACE}/${Constant.ARCHIVE_DIRECTORY}/${projectName}-${env}-${version}-unsigned.apk"
  } 
  else {
    def currentDate = new Date().format('ddMMyyyy')
    sh "cp ${Constant.APK_DEBUG_PATH}/android-debug.apk ${WORKSPACE}/${Constant.ARCHIVE_DIRECTORY}/${projectName}-${env}-${version}-${currentDate}.apk"
  }  
}

def iosArchive(env, name, mode) {
  def version = versionUtils.getFrontendVersion()

  if (mode.equals('debug')) {
    def currentDate = new Date().format('ddMMyyyy')

    zip archive: true, dir: 'platforms/ios', glob: '', zipFile: "xcode-${name}-${env}-${version}-${currentDate}.zip"
    return
  }

  zip archive: true, dir: 'platforms/ios', glob: '', zipFile: "xcode-${name}-${env}-${version}.zip"
}

def getMap(config) {
  def map = config.build ?: [:]
  def currentPkgId = getCurrentPkgId(config.directory)
  def defaultBranchName = env.BRANCH_NAME ?: Constant.DEV_BRANCH

  if (map.projectName == null) {
    throw new Exception("The project name is required. You need to configure the `projectName` attribute in the build map.")
  }
  
  map.sourceBranch = config.sourceBranch ?: defaultBranchName
  map.multipleAppVersions = map.multipleAppVersions ?: false
  map.devEnvironments = map.devEnvironments ?: ['dev', 'staging']
  map.deliveryEnvironments = map.deliveryEnvironments ?: ['uat', 'preprod', 'prod']
  map.packageId = map.packageId ?: currentPkgId
  map.builderTool = map.builderTool ?: 'npm'
  map.builderTool = evaluate("new ${map.builderTool}()")
  
  return map
}

def setExternalPkgId (env, packageId) {
  if (env in ['preprod', 'prod']) {
    sh "snow package ${packageId}"
    return
  }
}

def getBranches (String repoName) {
  return input(
    id: 'userInput', message: 'mobile build job required source branch input', parameters: [
      choice(name: 'sourceBranch', choices: gitUtils.getAllBranches(repoName), description: 'Source branch')
    ])
}

def getCurrentPkgId(directory = '') {
  return sh(script: "snow get-package --dir ${directory}", returnStdout: true).trim()
}

def getCurrentAppName(directory = '') {
  return sh(script: "snow get-name --dir ${directory}", returnStdout: true).trim()
}

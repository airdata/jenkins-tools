def build(Closure body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()
  
  def directory = config.directory
  def buildConfig = mobileUtils.getMap(config);
  def notifications = [buildKey: buildConfig.sourceBranch + '-android', buildName: buildConfig.sourceBranch + '-android']
  
  bitbucket.statusNotify(BuildStatus.INPROGRESS, notifications)
  try {
    stage('Mobile Android build') {
      dir(directory) {
        if(Constant.DELIVERY_BRANCH == buildConfig.sourceBranch) {
          def releaseCb = mobileUtils.androidReleaseMode()
          
          mobileUtils.buildAppEnvs {
            build = buildConfig
            envs = buildConfig.deliveryEnvironments
            callback = releaseCb
          }
        } else {
          def debugCb = mobileUtils.androidDebugMode()
          
          mobileUtils.buildAppEnvs {
            build = buildConfig
            envs = buildConfig.devEnvironments
            callback = debugCb
          }
        }
      }
    }

    bitbucket.statusNotify(BuildStatus.SUCCESSFUL, notifications)
  } catch(e) {
    bitbucket.statusNotify(BuildStatus.FAILED, notifications)
    throw new Exception("Build Fail", e)
  }
}

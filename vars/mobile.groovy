def build(Closure body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def sourceBranch = params.BUILD_BRANCH ?: null
  def repoName = config.repoName ?: config.projectName

  if (!sourceBranch) {
      node {
          try {
              stage('user input branch') {
                  def userInput = mobileUtils.getBranches(repoName)
                  sourceBranch = userInput.trim()
              }
          } finally {
              deleteDir()
          }
      }
  }

  parallel(
      'android': {
          if (config.build.android) {
              node('node8') {
                  try {
                        gitUtils.checkoutStageWithDist(sourceBranch)
                        config.build.android.projectName = config.projectName
                        buildAndroid(config, sourceBranch)
                        artifacts.archiveAll()
                    } finally {
                        deleteDir()
                  }
              }
          }
      },
      'ios': {
          if (config.build.ios) {
              node('mac') {
                  try {
                      gitUtils.checkoutStageWithDist(sourceBranch)
                      config.build.ios.projectName = config.projectName                  
                      buildIos(config, sourceBranch)
                  } finally {
                      deleteDir()
                  }
              }
          }
      }
  )
}

def buildAndroid(config, branchName) {
  android.build { 
    directory = config.dir
    sourceBranch = branchName
    build = config.build.android
  }
}

def buildIos(config, branchName) {
  ios.buildWithArchive {
    directory = config.dir
    sourceBranch = branchName
    build = config.build.ios
  }
}

def autoBuildProd(projectName, taskName="${projectName}-mobile") {
  if (Constant.DELIVERY_BRANCH == env.BRANCH_NAME) {
      build job: "../${projectName}-tasks/${taskName}",
        parameters: [
          string(name: 'BUILD_BRANCH', value: 'delivery')
        ],
        wait: false
  }
}

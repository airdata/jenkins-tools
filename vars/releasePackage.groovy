def call(body) {
  def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()

  def projectName = config.projectName
  def repoName = config.repoName ?: config.projectName
  def releases = config.releases

  def sourceBranch
  def nextVersion
  def mobileStoresVersion
  def tagName
  def backendRelease = releaseUtils.getBackendRelease(releases)
  def mobileRelease = releaseUtils.getMobileRelease(releases)

  job.discardBuilds(3)

  node {
      try {
          stage('user input branch') {
              def userInput = releaseUtils.userBranchInput(repoName)
              sourceBranch = userInput.trim()
          }

          gitUtils.checkoutStage(sourceBranch)

          stage('initialize version') {
              version = versionUtils.getCurrentVersion(releases)
          }

          stage('upgrade version') {
              if (releaseUtils.isMaintenanceBranch(sourceBranch).find()) {
                  nextVersion = versionUtils.upgradePatch(version)
              }
              else {
                  def versionUserInput = releaseUtils.mainReleaseInput(version).trim()

                  version = (versionUserInput == version) ? version : versionUserInput
                  nextVersion = versionUtils.upgradeMinor(version)
              }

              if (mobileRelease != null) {
                  mobileStoresVersion = releaseUtils.mobileVersionInput(mobileRelease.dir)
              }
              
              tagName = projectName + '-' + version
          }

          stage('setting versions') {
              versionUtils.setVersions(releases, version, mobileStoresVersion)
          }

          gitUtils.createTagStage(tagName)
          gitUtils.updateDeliveryStage(Constant.DELIVERY_BRANCH, tagName)

          if (backendRelease) {
              stage('prepare next version') {
                  def nextVersionSnapshot = "${nextVersion}-SNAPSHOT"
                  
                  versionUtils.setBackendVersion(nextVersionSnapshot, backendRelease.dir)
                  gitUtils.pushNextVersionStage(nextVersionSnapshot, sourceBranch)
              }
          }
      } finally {
          cleanWs()
      }
  }
}


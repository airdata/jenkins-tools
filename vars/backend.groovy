def build(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    def directory = config.directory
    def externalServiceDirectories = config.externalServiceDirectories ?: []
    def buildMap = getBuildMap(config)
    
    bitbucket.statusNotify(BuildStatus.INPROGRESS, buildMap.notifications)
    try {
        if(!externalServiceDirectories.isEmpty()) {
            stage('External Services Build') {
                externalServiceStages = [:]
                externalServiceDirectories.each { String serviceDirectory ->
                    externalServiceStages[serviceDirectory] = {
                        dir(serviceDirectory) {
                            stage(serviceDirectory) {
                                maven.noStageInstall()
                            }
                        }
                    }
                }
                parallel(externalServiceStages)
            }
        }

        if (directory) {
            dir(directory) {
                packageAndArchive(buildMap, directory)
            }
        } else {
            packageAndArchive(buildMap, directory)
        }

        bitbucket.statusNotify(BuildStatus.SUCCESSFUL, buildMap.notifications)
    } catch (e) {
        bitbucket.statusNotify(BuildStatus.FAILED, buildMap.notifications)
        throw new Exception("Build Fail", e)
    }
}

private def getBuildMap(config) {
    def buildMap = config.build ?: [:]
    def buildProfile = env.BRANCH_NAME == Constant.DELIVERY_BRANCH ? 'prod' : 'dev'
    def defaultBuildParams = [
      '*': [skipTests: true]
    ]

    buildMap.profiles = buildMap.profiles ?: buildProfile
    buildMap.params = buildMap.params ? defaultBuildParams << buildMap.params : defaultBuildParams
    buildMap.test = buildMap.test ?: [ skipTests: false ]
    buildMap.frontendBuild = buildMap.frontendBuild ?: false
    buildMap.notifications = buildMap.notifications ?: [ buildKey: env.BRANCH_NAME, buildName: env.BRANCH_NAME ]
    buildMap.sonar = buildMap.sonar ?: [enabled: true, profiles: ""]
    buildMap.archiveParams = buildMap.archiveParams ? [doArchive: true] << buildMap.archiveParams : [doArchive: true]
    buildMap.deployNexus = buildMap.deployNexus ?: null
    buildMap.devBranch = buildMap.devBranch ?: Constant.DEV_BRANCH
    
    return buildMap
}

private def packageAndArchive(buildMap, directory) {
    if(buildMap.frontendBuild != null && buildMap.frontendBuild == true) {
        stage("Back ${directory?:''} frontend Prepare") {
            npm.install( [skipTests: true] )
            maven.clean()
        }
    }
    
    stage("Backend Compilation ${directory?:''}") {
        maven.compile()
    }
    
    if(buildMap.test.skipTests == false) {
        stage("Backend ${directory?:''} test") {
            maven.test(buildMap.test)
        }
    }

    if(buildMap.sonar.enabled && env.BRANCH_NAME == buildMap.devBranch) {
        maven.sonar(buildMap.sonar.profiles)
    }
    
    maven.pack(buildMap.profiles, buildMap.params)
    
    if(buildMap.deployNexus && (env.BRANCH_NAME == buildMap.devBranch || env.BRANCH_NAME == Constant.DELIVERY_BRANCH) ) {
        maven.deploy()
    }
    
    archive(buildMap.archiveParams)
}

private def archive(params) {
    if(params.doArchive) {
        def packaging = maven.eval('project.packaging')
        def destinationDirectory = "${env.WORKSPACE}/${Constant.ARCHIVE_DIRECTORY}/"

        sh "cp target/*.${packaging} ${destinationDirectory}"

        if (params.archiveDir) {
          sh "mv ${WORKSPACE}/${params.archiveDir}/*.yml ${destinationDirectory}"
          return
        }

        sh "mv src/main/resources/config/*.yml ${destinationDirectory}"
    }
}
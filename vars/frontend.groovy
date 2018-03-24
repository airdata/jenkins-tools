def build(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    def directory = config.directory
    def buildMap = getBuildMap(config)
    def installDeps = config.installDeps == null ? true : config.installDeps

    bitbucket.statusNotify(BuildStatus.INPROGRESS, buildMap.notifications)
    try {
        if (installDeps) {
            stage("Frontend install ${directory?:''}") {
                dir(directory) {
                    buildMap.builderTool.install(buildMap.test)
                }
            }
        }

        stage("Frontend build ${directory?:''}") {
            dir(directory) {
                buildEnvs(buildMap)
            }
        }
        bitbucket.statusNotify(BuildStatus.SUCCESSFUL, buildMap.notifications)
    } catch (e) {
        bitbucket.statusNotify(BuildStatus.FAILED, buildMap.notifications)
        throw new Exception("Build failed", e);
    }
}

private def getBuildMap(config) {
    def buildMap = config.build ?: [:]
    buildMap.dev = buildMap.dev ?: [ environments: ['dev', 'staging'] ]
    buildMap.test = buildMap.test ?: [ skipTests: false ]
    buildMap.notifications = buildMap.notifications ?: [ buildKey: env.BRANCH_NAME, buildName: env.BRANCH_NAME ]
    buildMap.builderTool = buildMap.builderTool ?: 'npm'
    buildMap.builderTool = evaluate("new ${buildMap.builderTool}()")
    
    return buildMap
}

private def buildEnvs(buildMap) {
    if(buildMap.skipEnvironments != null && buildMap.skipEnvironments == true) {
        buildAndArchive(buildMap.builderTool)
        return
    }
    
    if(Constant.DELIVERY_BRANCH == env.BRANCH_NAME) {
        def delivery = buildMap.delivery
        
        buildEach(buildMap.builderTool, delivery.environments, delivery.options)
        return
    }

    buildEach(buildMap.builderTool, buildMap.dev.environments, buildMap.dev.options)
}

private def buildEach(builderTool, environments, options = "") {
    environments.each { env ->
        buildAndArchive(builderTool, env, options)
    }
}

private void buildAndArchive(builderTool, env = null, options = null) {
    builderTool.build(env, options)
    archive()
}

private def archive() {
    def destinationDirectory = "${WORKSPACE}/${Constant.ARCHIVE_DIRECTORY}"  
    sh "mv dist/*.zip ${destinationDirectory}"
}

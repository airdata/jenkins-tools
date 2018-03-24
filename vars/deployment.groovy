def pipeline(deployMap) {
    if(deployMap.type && deployMap.type == 'docker') {
        dockerTool.deploy( deployMap )        
    } else {
        node {
            copyFrom(deployMap.config, deployMap.params)

            deploy(deployMap)

            stage('Clean up') {
                cleanWs()
            }
        }
    }
}

def deploy(deployMap) {
    def lowercaseProjectName = deployMap.config.projectName.toLowerCase()
    String envName = deployMap.params.BUILD_ENV
    def envChoice = deployMap.config.environments[ envName ]
    def strictHostKeyChecking = deployMap.config.strictHostKeyChecking ?: true;
    
    deployMap.config.deployments.each { deployName, deployConf ->
        stage("deploy-${deployName}") {
            sshagent(credentials: ['jenkins_ssh']) {
                dir("${Constant.ARCHIVE_DIRECTORY}") {
                    executeDeployScript(lowercaseProjectName, deployConf, envChoice, envName, strictHostKeyChecking)
                }
            }
        }       
    }
}

def executeDeployScript(projectName, deployConf, envChoice, envName, strictHostKeyChecking) {
    def script = resolveScript(deployConf);
    def strictHostKeyCheckingScript = strictHostKeyChecking ? '' : '-o StrictHostKeyChecking=no'
    def customProjectName = deployConf.projectName ?: projectName

    def artifact = sh(script: "basename ${deployConf.file}", returnStdout: true).trim()
    sh("scp ${strictHostKeyCheckingScript} ./${artifact} bootapp@${envChoice.server}:/opt/${projectName}/archive/")

    if(script == 'redeploy-yml.sh') {
        sh("ssh bootapp@${envChoice.server} /opt/${projectName}/sbin/${script} ${customProjectName} ${envName} /opt/${projectName}/archive/${artifact}")
    } else {
        sh("ssh bootapp@${envChoice.server} /opt/${projectName}/sbin/${script} ${customProjectName} /opt/${projectName}/archive/${artifact}")
    }
    
}

private def resolveScript(deployConf) {
    switch (deployConf.type) {
        case 'frontend' : return 'redeploy-frontend.sh'
        case 'backend' : return 'redeploy-backend.sh'
        case 'back-office' : return 'redeploy-back-office.sh'
        case 'yml' : return 'redeploy-yml.sh'
        default: throw new IllegalArgumentException("Unknown deployment type : ${deployConf.type}")
    }
}

def copyFrom(config, params) {
    config.devBranch = config.devBranch ?: Constant.DEV_BRANCH
    def selector = getSelector(params)

    stage('Define deployment parameters') {
        def deployEnvironment = getSourceBranch(params, config)
        config.pipeline = "../${config.projectName}-pipeline/${deployEnvironment}"

        logDeploymentInfo(config, params)
    }

    artifacts.copy(config.pipeline, selector)
}

private def getSelector(params) {
    return params.BUILD_ENV == 'dev' ? devCopySelector() : deliveryCopySelector(params)
}

private def deliveryCopySelector(params) {
    return params.ARCHIVE_BUILD_NUMBER?.isInteger() ? 
            [$class: 'SpecificBuildSelector', buildNumber: params.ARCHIVE_BUILD_NUMBER] : 
            devCopySelector()
}

private def devCopySelector() {
    return [$class: 'StatusBuildSelector', stable: true]
}

private def getSourceBranch(params, config) {
    switch (params.BUILD_ENV) {
        case 'dev':  return config.devBranch
        case ~/.*staging.*/ :   return askSourceBranch(config.devBranch)
        default:                return Constant.DELIVERY_BRANCH
    }
}

private def askSourceBranch(devBranch) {
    return input(
            id: 'development or delivery branch', message: 'Which branch to deploy ? development or delivery ?', parameters: [
            choice(name: 'sourceBranch', choices: "${devBranch}\ndelivery", description: 'Branches')
    ])
}

private def logDeploymentInfo(config, params) {
    echo "deployment env : ${params.BUILD_ENV}"
    echo "pipeline to retrieve artifacts : ${config.pipeline}"
    echo "build number : ${params.ARCHIVE_BUILD_NUMBER}"
}

def deploy(deployMap) {
    node('docker') {
        def projectName = deployMap.config.projectName
        def options = deployMap.config.options

        stage("Pull latest image") {
            ssh("docker pull ${Constant.DOCKER_REGISTRY}/${projectName}:latest")
        }

        stage("Stop current container") {
            ssh("docker stop ${projectName} || true")
        }

        stage("Remove current container") {
            ssh("docker rm ${projectName} || true")
        }

        stage("Remove current image") {
            ssh("docker rmi ${Constant.DOCKER_REGISTRY}/${projectName}:current || true")
        }

        stage("Tag new current image") {
            ssh("docker tag ${Constant.DOCKER_REGISTRY}/${projectName}:latest ${Constant.DOCKER_REGISTRY}/${projectName}:current")
        }

        stage("Run new image") {
            ssh("docker run ${options} --name='${projectName}' -d ${Constant.DOCKER_REGISTRY}/${projectName}:latest")
        }

        stage('Clean up') {
            cleanWs()
        }
    }
}

private def ssh(String script) {
    sh ("ssh bootapp@${Constant.DOCKER_HOST} " + script)
}


def build(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    def dockerImage
    
    stage('Docker build') {
        def projectName = getProjectName(config)
        
        dockerImage = docker.build("${Constant.DOCKER_REGISTRY}/${projectName}")
    }
    
    stage('Docker push') {
        dockerImage.push()
    }
}

private String getProjectName(config) {
    Closure evalProjectName = { maven.eval('project.artifactId') }
    
    if (config.backendDirectory) {
        dir(config.backendDirectory) {
            return evalProjectName()
        }
    } else {
        return evalProjectName()
    }
}
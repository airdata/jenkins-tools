This repository contains Groovy code that allows to perform docker build and docker push by only having the "docker cli" installed, no need to install Docker plugins.

This code should be imported as Shared Library in Jenkins. An example of use is:

```# 
@Library('jenkins-shared-lib') _

pipeline {
    agent {
        label "master"
    }
    

    
    stages {
        stage("run") {
            steps {
                script {
                    echo "running...";
                    // This is a repo with a single Dockerfile 
                    git 'https://github.com/danielalejandrohc/docker-dummy-repo.git';
                    // This is a mapping configuration with multiple Docker registries. This will allow to perform more comple docker build operations.
                    registriesConf = [
                        [
                            "credentialId": "azureCredentialsId",
                            "registry": "fooRepo.io"
                        ],
                        [
                            "credentialId": "dockerhubCredentialsId",
                            "registry": "registry.hub.docker.com"
                        ]
                    ]
                    // Default build. This will pull 'nginx:latest' image from DockerHub and tag it as 'fooRepo.io/fooOrg/nginx:latest'
                    dockerBuild registriesConf: registriesConf, registry: 'fooRepo.io', registryOrg: 'fooOrg', imageName: 'nginx';
                    // Default build. This will pull 'nginx:1.17.3-alpine' image from DockerHub and tag it as 'fooRepo.io/fooOrg/nginx:1.17.3-alpine'
                    dockerBuild registriesConf: registriesConf, registry: 'fooRepo.io', registryOrg: 'fooOrg', imageName: 'nginx', dockerTag: "1.17.3-alpine";
                    // This will build the path of 'dockerFile' parameter and tag it as 'fooRepo.io/fooOrg/nginx'
                    dockerBuild registriesConf: registriesConf, registry: 'fooRepo.io', registryOrg: 'fooOrg', imageName: 'nginx', dockerFile: "Dockerfile", path: ".";
                    
                    // Build jenkins/jenkins (Official repo for Jenkins images)
                    dockerBuild registriesConf: registriesConf, registry: 'fooRepo.io', registryOrg: 'fooOrg', imageName: 'jenkins';
                    // Build jenkins (Docker image certified)
                    dockerBuild registriesConf: registriesConf, registry: 'fooRepo.io', registryOrg: 'fooOrg', imageName: 'jenkins/jenkins';

                    // Pushing images
                    dockerPush credentialsId: 'azureCredentialsId', registry: 'fooRepo.io', registryOrg: 'fooOrg', imageName: 'nginx';
                    dockerPush credentialsId: 'azureCredentialsId', registry: 'fooRepo.io', registryOrg: 'fooOrg', imageName: 'jenkins/jenkins';
                    dockerPush credentialsId: 'azureCredentialsId', registry: 'fooRepo.io', registryOrg: 'fooOrg', imageName: 'jenkins';
                }
            }
        }
    }
}

```

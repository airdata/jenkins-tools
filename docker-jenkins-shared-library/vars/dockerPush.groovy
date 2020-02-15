
/*
    This method is to build a docker image
    'credentialsId': This is a credential id of type "User and password"
    'registry': This is docker registry. i.e registry.hub.docker.com
    'registryOrg': This is docker organization name in the registry
    'imageName': This is the name of the image
    'dockerTag': (Optional) If not specified the default value will be: 'latest'
*/
def call(args) {
    def MANDATORY_ARGS = ['credentialId', 'registry', 'registryOrg', 'imageName']

    // Check for missing (mandatory) parameters
    def given_args = args.keySet();
    MANDATORY_ARGS.each {
        mandatoryArg ->
            if(!args.containsKey(mandatoryArg))
                error "Error: ${mandatoryArg} is missing. Custom step: 'dockerBuild'"
    }

    // Assign default values
    // If 'dockerfile' parameter does not exists then It will assign the default dockerfile name: 'Dockerfile'
    if(!args.containsKey("dockerFile")) {
        args.dockerFile = "Dockerfile";
    }


    if(!args.containsKey("dockerTag")) {
        args.dockerTag = "latest";
    }

    if(!args.containsKey("deleteImages")) {
        args.deleteImages = false;
    }

    echo "Arguments: ${args}";        
           
    withCredentials([usernamePassword(credentialsId: args.credentialId, passwordVariable: 'password', usernameVariable: 'username')]) {
        try {
            sh "docker login -u ${username} -p ${password} https://${args.registry}";
            echo "Docker login perfomed."
        } catch(e) {
            error "Error docker login ${e}"
        }
    }
    sh "docker tag ${args.registryOrg}/${args.imageName}:${args.dockerTag} ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag}";
    sh "docker push ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag}";
    
    // If deleteImages parameter is passed as true the docker rmi will be performed
    if(args.deleteImages) {
        sh "docker rmi ${args.registry}/${args.registryOrg}/${args.imageName}:${args.dockerTag}";
        // Delete images from dockerhub no tagged with organization nor registry. If found then It will be deleted.
        output = sh script: "docker images -q ${args.imageName}:${args.dockerTag}", returnStdout: true;
        echo "Output: ${output}";
        
        if(!output.trim().isEmpty()) {
            sh "docker rmi ${args.imageName}:${args.dockerTag}";
        }
    }
    
}

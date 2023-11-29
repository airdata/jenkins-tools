/*
    This method is to build a docker image
    'registriesConf': This is a configuration mapping to allow to resolve against all these regisrties a single docker build. These allows to combine Docker registries when the 'docker build ...' is performed
    'registry': This is docker registry. i.e registry.hub.docker.com. This is the registry that will be used to perform the tag
    'registryOrg': This is docker organization name in the registry
    'imageName': This is the name of the image
    'dockerFile': (Optional) If not specified the default value will be: 'Dockerfile'
    'path': (Optional) If not specified the default value will be:  '.'
    'dockerTag': (Optional) If not specified the default value will be: 'latest'
*/
def call(args) {
    def DOCKER_HUB_REGISTRY = "registry.hub.docker.com";
    def MANDATORY_ARGS = ['credentialId', 'registry', 'registryOrg', 'imageName'];

    // Check If missing (mandatory) parameters
    def given_args = args.keySet();
    MANDATORY_ARGS.each {
        mandatoryArg ->
            if(!args.containsKey(mandatoryArg))
                error "Error: ${mandatoryArg} is missing. Custom step: 'acrBuild'";
    }

    // If 'path' parameter does not exists then It will assign the default path, which is the current path: '.'
    if(!args.containsKey("path")) {
        args.path = ".";
    }

    if(!args.containsKey("dockerTag")) {
        args.dockerTag = "latest";
    }

    echo "Arguments: ${args}";        
    
    withCredentials([azureServicePrincipal(
                        clientIdVariable: 'AZURE_CLIENT_ID_VAR', 
                        clientSecretVariable: 'AZURE_CLIENT_SECRET_VAR', 
                        credentialsId: args.credentialId, 
                        subscriptionIdVariable: 'AZURE_SUBSCRIPTION_ID_VAR', 
                        tenantIdVariable: 'AZURE_TENANT_ID_VAR')]) {
        sh "az login --service-principal -u $AZURE_CLIENT_ID_VAR -p $AZURE_CLIENT_SECRET_VAR -t $AZURE_TENANT_ID_VAR";
        // This will split an String such as: acrliq001.azurecr.io to [acrliq001, azurecr, io]
        registrySplit = registry.split("\\.");
        sh "az acr login --name ${registrySplit[0]}";
        echo "Docker login performed";
    }
        
    if(!args.containsKey("dockerFile")) {
        // When no 'dockerFile' parameter is provided but 'imageName' is found the it will pull the image from Dockerhub and tag it with the registry provided
        sh "docker pull ${args.imageName}:${args.dockerTag}";
        sh "docker tag ${args.imageName}:${args.dockerTag} ${args.registryOrg}/${args.imageName}:${args.dockerTag}"
    } else {
        // If the parameter 'dockerFile' is provided then it will build the file
        sh "docker build -f ${args.dockerFile} -t ${args.registryOrg}/${args.imageName}:${args.dockerTag} -t ${args.dockerTag} ${args.path}";
    }    
}

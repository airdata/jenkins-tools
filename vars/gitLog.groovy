def commitMessage (){
    commitMessage = sh(script: "git log -1 --pretty=format:%s", returnStdout: true).trim() as String
    return commitMessage
}

def committerEmail (){
    committerEmail = sh(script: "git log -1 --pretty=format:%ce", returnStdout: true).trim() as String
    return committerEmail
}
def defaultRecepients () {
    datas = readYaml (file: "${env.WORKSPACE}/vars/${ENV}_vars.yml")
    defaultRecepients=datas.emailRecipients.toString()
    return committerEmail
}

def gitCheckout (){
    git branch: '${BRANCH}',
        credentialsId: 'GIT_CREDS',
        url: 'https://${REPO}'
}
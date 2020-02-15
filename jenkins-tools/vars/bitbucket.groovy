def statusNotify(BuildStatus status, LinkedHashMap parameters) {
    bitbucketStatusNotify ( buildState: "${status}", buildKey: parameters.buildKey, buildName: parameters.buildName )
}

def statusNotify(String status, parameters) {
    statusNotify(BuildStatus.valueOf(status), parameters)
}
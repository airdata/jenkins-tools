#!/usr/bin/env groovy

/**
* This method is a part of the CEE Shared Library project
* @return One of the possible outputs specified in dockerlint.txt
*/
def call(Map args = [:]) {

    //Store Constants: Mandatory Arguments, Dockerfile name
    final List<String> MANDATORY_ARGS = ['path']

    //Check for missing (mandatory) parameters
    def given_args = args.keySet();
    def missing_args = MANDATORY_ARGS - given_args;
    if (!missing_args.isEmpty()) {
        error("[CEE Jenkins Shared Library] Missing Arguments: ${missing_args}")
    }

    //Stage which executes dockerlint
    stage('Execute dockerlint test') {
        sh "dockerlint ${args['path']}";
    }
}

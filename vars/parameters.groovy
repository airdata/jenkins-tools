import groovy.json.JsonSlurper
def call() {
    properties([
        parameters([
            string(
                defaultValue: 'main',
                description: 'Select the branch to deploy from',
                name: 'BRANCH'),
            choice(
                choices: ['dev', 'test','prod'],
                description: 'Select the environment to deploy',
                name: 'ENV'),
                [
                $class: 'ChoiceParameter',
                choiceType: 'PT_SINGLE_SELECT',
                filterLength: 1,
                filterable: false,
                name: 'VER',
                randomName: 'choice-parameter-1898886208177958',
                script:
                [
                    $class: 'GroovyScript',
                    fallbackScript: [
                        classpath: [],
                        sandbox: false,
                        script: ''],
                        script: [
                            classpath: [],
                            sandbox: false,
                            script: 'nexusDockerImage("http://172.17.0.3:8081/repository/docker/v2/repository/docker/alpine/tags/list")'
            ]
          ]
        ]
      ])
    ])
}
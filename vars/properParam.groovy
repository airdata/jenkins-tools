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
                            script: '''import groovy.json.JsonSlurper
def nexusURL = "http://172.17.0.3:8081/repository/docker/v2/repository/docker/alpine/tags/list"
def nexusAPIResponse = new URL(nexusURL).text;
def nexusAPIResponseSlurper = [:]
nexusAPIResponseSlurper = new JsonSlurper().parseText(nexusAPIResponse)
def continuationToken = nexusAPIResponseSlurper.continuationToken

def image_tag_list = []
nexusAPIResponseSlurper.tags.each { tag_metadata ->
 image_tag_list.add(tag_metadata)
}

try {
 while(continuationToken != 'null'){
   def nexusAPIResponseWithToken = new URL("${nexusURL}&continuationToken=${continuationToken}").text;
   println nexusAPIResponseWithToken
   def nexusAPISlurperWithToken = [:]
   def nexusAPIResponseSlurperWithToken = new JsonSlurper().parseText(nexusAPIResponseWithToken)
   continuationToken = nexusAPIResponseSlurperWithToken.continuationToken
   nexusAPIResponseSlurperWithToken.tags.each { tag_metadata ->
     image_tag_list.add(tag_metadata)
   }
 }
}
catch(Exception e){
 println(e)
}
return image_tag_list'''
            ]
          ]
        ],
                [
                $class: 'ChoiceParameter',
                choiceType: 'PT_SINGLE_SELECT',
                filterLength: 1,
                filterable: false,
                name: 'VER2',
                randomName: 'choice-parameter-2898886208177958',
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
                            script: scriptOfSource(),
            ]
          ]
        ]
      ])
    ])
}


@NonCPS
private String scriptOfSource() {
    def nexusURL = "http://172.17.0.3:8081/repository/docker/v2/repository/docker/alpine/tags/list"
    def nexusAPIResponse = new URL(nexusURL).text;
    def nexusAPIResponseSlurper = [:]
    nexusAPIResponseSlurper = new JsonSlurper().parseText(nexusAPIResponse)
    def continuationToken = nexusAPIResponseSlurper.continuationToken

    def image_tag_list = []
    nexusAPIResponseSlurper.tags.each { tag_metadata ->
     image_tag_list.add(tag_metadata)
    }

    try {
     while(continuationToken != 'null'){
       def nexusAPIResponseWithToken = new URL("${nexusURL}&continuationToken=${continuationToken}").text;
       println nexusAPIResponseWithToken
       def nexusAPISlurperWithToken = [:]
       def nexusAPIResponseSlurperWithToken = new JsonSlurper().parseText(nexusAPIResponseWithToken)
       continuationToken = nexusAPIResponseSlurperWithToken.continuationToken
       nexusAPIResponseSlurperWithToken.tags.each { tag_metadata ->
         image_tag_list.add(tag_metadata)
       }
     }
    }
    catch(Exception e){
     println(e)
    }
    return image_tag_list
}

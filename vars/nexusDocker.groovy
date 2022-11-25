import groovy.json.JsonSlurper
/**
* nexusurl should be something like this http://172.17.0.3:8081/repository/docker/v2/repository/docker/alpine/tags/list
*/
def call (String nexusurl) {
    def nexusURL = nexusurl
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
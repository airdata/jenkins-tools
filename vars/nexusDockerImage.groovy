import groovy.json.JsonSlurper

def test(nexusURL){
  // Import the JsonSlurper class to parse Dockerhub API response
  // Set the URL we want to read from, it is MySQL from official Library for this example, limited to 20 results only.
  docker_image_tags_url = nexusURL
  try {
      // Set requirements for the HTTP GET request, you can add Content-Type headers and so on...
      def http_client = new URL(docker_image_tags_url).openConnection() as HttpURLConnection
      http_client.setRequestMethod('GET')
      
      // Run the HTTP request
      http_client.connect()
      // Prepare a variable where we save parsed JSON as a HashMap, it's good for our use case, as we just need the 'name' of each tag.
      def dockerhub_response = [:]    
      // Check if we got HTTP 200, otherwise exit
      if (http_client.responseCode == 200) {
          dockerhub_response = new JsonSlurper().parseText(http_client.inputStream.getText('UTF-8'))
      } else {
          println("HTTP response error")
          System.exit(0)
      }
      // Prepare a List to collect the tag names into
      def image_tag_list = []
      // Iterate the HashMap of all Tags and grab only their "names" into our List
      dockerhub_response.tags.each { tag_metadata ->
          image_tag_list.add(tag_metadata)    
      }
      // The returned value MUST be a Groovy type of List or a related type (inherited from List)
      // It is necessary for the Active Choice plugin to display results in a combo-box
      return image_tag_list.sort()
  } catch (Exception e) {
           // handle exceptions like timeout, connection errors, etc.
           println(e)
  }
}
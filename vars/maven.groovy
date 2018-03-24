def clean() {
    sh "mvn clean"
}

def compile() {
    sh "mvn compile"
}

def noStageInstall() {
    sh "mvn -T C1.5 install"
}

def install() {
    stage('Backend install') {
        sh "mvn -T C1.5 install"
    }
}

def pack(profiles = 'dev', parameters) {
    stage('Backend packaging') {
      def command = "mvn -T C1.5 package -P${profiles}"

      parameters = cleanParams(parameters, profiles)
      sh "${ addParameters(command, parameters) }"
    }
}

def cleanParams (parameters, profile) {
  def params = [:]
  parameters.each { key, value -> 
    if (key == '*' || key == profile) {
      params = params << value
    }
  }

  return params
}

def deploy() {
    stage('Deploy artifact') {
        sh "mvn deploy -Dmaven.install.skip=true -Dmaven.main.skip=true -DskipTests"
    }
}

def sonar(profiles = "") {
    profiles = profiles ?: ""
    stage('Sonar Reporting') {
        def command = "mvn sonar:sonar"
        def profilesParam = profiles.isEmpty() ? [:] : ["${profiles}": null]
        sh "${ addParameters(command, profilesParam) }"
    }
}

def test(parameters) {
    def command = "mvn -T C1.5 test"

    sh "${ addParameters(command, parameters) }"
    
    if(parameters.resultsPattern) {
        junitArchiver.archive( parameters.resultsPattern )
    }
}

private def addParameters(command, parameters) {
    def isMvnPkgCmd = command.contains('mvn') && command.contains('package')
    parameters = parameters ?: [:]

    parameters.each { key, value ->
        if (key == 'resultsPattern' || (key == 'skipTests' && !isMvnPkgCmd)) return
        
        def additionalParameter = value ? "-D${key}=${value}" : "-D${key}"
        command += " ${additionalParameter}"
    }
    return command
}

def evalPomVersion() {
    def pom = readMavenPom(file: 'pom.xml')
    return pom.version
}

def eval(expression) {
    def stringExpression = "${expression}"
    return sh(script: "mvn -q -Dexec.executable='echo' -Dexec.args=\'\${$stringExpression}\' --non-recursive exec:exec", returnStdout: true).trim()
}
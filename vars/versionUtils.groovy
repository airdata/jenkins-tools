def getCurrentVersion(releases) {
  def backend = releaseUtils.getBackendRelease(releases)
  if (backend) {
    return getBackendVersion(backend.dir)
  }

  def frontend = releaseUtils.getReleaseByType(releases, 'npm')
  if (frontend) {
    return getFrontendVersion(frontend.dir)
  }
}

def setVersions(releases, version, mobileVersion) {
  releases.each { release ->
    if (release.type == 'mvn') {
      setBackendVersion(version, release.dir)
    }

    if (release.type == 'npm') {
      setFrontEndVersion(version, release.dir)
    }

    if (release.type == 'cordova') {
      setFrontEndVersion(version, release.dir)
      setMobileVersion(mobileVersion, release.dir)
    }
  }
}

def getBackendVersion(String backDir) {
  if (!backDir) {
    return getMvnVersion()
  }
  
  dir(backDir) {
    return getMvnVersion()
  }
}

def getFrontendVersion (String frontDir = null) {
  if (!frontDir) {
    return getNpmVersion()
  }

  dir(frontDir) {
    return getNpmVersion()
  }
}

def setBackendVersion(String version, String directory) {
  if (!directory) {
    setMvnVersion(version)
    return
  }

  dir(directory) {
    setMvnVersion(version)
  }
}

def setFrontEndVersion(String version, String directory) {
  if (!directory) {
    setNpmVersion(version)
    return
  }

  dir(directory){
    setNpmVersion(version)
  }
}

def getMobileVersion(String dir) {
  return sh(script: "snow get-version --dir ${dir}", returnStdout: true).trim()
}

def upgradePatch (String version) {
  return upgradeVersion(version, 2)
}

def upgradeMinor (String version) {
  return upgradeVersion(version, 1)
}

private def upgradeVersion (String version, Integer index) {
  def convertVersion = version.split('\\.')
  convertVersion[index] = (convertVersion[index].toInteger() + 1).toString()
  return convertVersion.join('.')
}

private def getNpmVersion () {
  return sh(script: 'node -p "require(\'./package.json\').version"', returnStdout: true).trim()
}

private def getMvnVersion () {
  def pomVersion = maven.evalPomVersion()
  return pomVersion.endsWith('-SNAPSHOT') ? pomVersion.substring(0, pomVersion.length() - '-SNAPSHOT'.length()) : pomVersion  
}

private def setMvnVersion(version) {
  sh "mvn versions:set -DnewVersion=${version}"
  sh "git add '*pom.xml'"
}

private def setNpmVersion (version) {
  sh "npm version ${version}"
  sh "git add package.json" 
}

private def setMobileVersion(version, dir) {
  def currentVersion = getMobileVersion(dir)
  dir = dir ?: '.'

  if (currentVersion != version) {
    sh "snow version ${version} --dir ${dir}"
    sh "git add ${dir}/config.xml"
  }
}
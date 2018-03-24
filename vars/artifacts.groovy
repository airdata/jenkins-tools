def createDist() {
    stage('Prepare folders') {
        sh "rm -Rf ${Constant.ARCHIVE_DIRECTORY} && mkdir ${Constant.ARCHIVE_DIRECTORY}"
    }
}

def archiveAll() {
    stage('Artifacts') {
        archiveArtifacts artifacts: "${Constant.ARCHIVE_DIRECTORY}/*"
    }
}

def copy(pipeline, selector) {
    stage('Copy Artifacts') {
        step([$class: 'CopyArtifact',
              projectName: pipeline,
              filter: "${Constant.ARCHIVE_DIRECTORY}/*",
              selector: selector
        ])
    }
}
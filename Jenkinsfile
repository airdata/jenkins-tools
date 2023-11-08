pipeline {
    agent any

    stages {
        stage('Pull Request Event') {
            when {
                // This stage runs when a pull request is opened or updated
                expression { currentBuild.branch == 'PR-*' }
            }
            steps {
                // Your pull request event processing steps go here
                sh 'echo "Processing pull request event"'
            }
        }

        stage('Main Branch Event') {
            when {
                // This stage runs when the main branch is updated
                expression { currentBuild.branch == 'master' }
            }
            steps {
                // Your main branch event processing steps go here
                sh 'echo "Processing main  branch event"'
            }
        }
    }
}

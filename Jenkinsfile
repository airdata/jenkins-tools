pipeline {
    agent any

    stages {
        stage('Pull Request Event') {
            when {
                // This stage runs when a pull request is opened or updated
                expression { env.BRANCH_NAME.startsWith('PR-') }
            }
            steps {
                // Your pull request event processing steps go here
                sh 'echo "Processing pull BRANCH_NAME.startsWith"'
            }
        }

        stage('Main Branch Event') {
            when {
                // This stage runs when the main branch is updated
                expression { currentBuild.branch == 'master' }
            }
            steps {
                // Your main branch event processing steps go here
                sh 'echo "Processing main  currentBuild.branch"'
            }
        }
    }
}

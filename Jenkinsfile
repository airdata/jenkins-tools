pipeline {
    agent any

    options {
        // Define options as needed
        skipDefaultCheckout(true) // Skip default checkout to customize SCM behavior
    }

    triggers {
        githubPullRequests(
            forks: true, 
            branches: [[compareType: 'REGEXP', compare: '.*', pattern: '.*']]
        ) // Trigger on pull request events
    }
    
    stages {
        stage('Pull Request Event') {
            when {
                // This stage runs when a pull request is opened or updated
                expression { env.BRANCH_NAME.startsWith('PR-') }
            }
            steps {
                // Your pull request event processing steps go here
                sh 'echo "Processing pull BRANCH_NAME.startsWith PR-"'
            }
        }

        stage('Main Branch Event') {
            when {
                // This stage runs when the main branch is updated
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                // Your main branch event processing steps go here
                sh 'echo "Processing main  env.BRANCH_NAME == master"'
            }
        }
    }

    post {
        success {
            echo 'Build and test succeeded!'
        }
        failure {
            echo 'Build or test failed!'
        }
    }
}

pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Checkout the source code from the Git repository
                checkout scm
            }
        }

        stage('Build') {
            steps {
                // Use the configured Maven tool
                script {
                    def mvnHome = tool 'MavenTool1'
                    bat "${mvnHome}/bin/mvn clean install"
                }
            }
        }

        stage('Test') {
            steps {
                // Use the configured Maven tool
                script {
                    def mvnHome = tool 'MavenTool1'
                    bat "${mvnHome}/bin/mvn test"
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'skipping deployment'
            }
        }
    }

    post {
        success {
            echo 'Build and tests succeeded! Deploying...'
            // Add deployment steps here if needed
        }

        failure {
            echo 'Build or tests failed! Notify the team...'
        }
    }
}

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
                // Build the Maven project
                script {
                    def mvnHome = tool 'Maven'
                    sh "${mvnHome}/bin/mvn clean install"
                }
            }
        }

        stage('Test') {
            steps {
                // Run tests using Maven
                script {
                    def mvnHome = tool 'Maven'
                    sh "${mvnHome}/bin/mvn test"
                }
            }
        }

        stage('Deploy') {
            steps {
                echo('skipping deployment')
            }
        }
    }

    post {
        success {
            // Actions to be taken if the build and tests succeed
            echo 'Build and tests succeeded! Deploying...'
            // Add deployment steps here if needed
        }

        failure {
            // Actions to be taken if the build or tests fail
            echo 'Build or tests failed! Notify the team...'
        }
    }
}

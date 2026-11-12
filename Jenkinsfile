// Runs the Maven Wrapper; no Maven install needed (JDK 17 required on the agent).
def mvnw(String args) {
    sh "./mvnw -B ${args}"
}

pipeline {
    agent any

    triggers {
        pollSCM('* * * * *')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                mvnw('test')
            }
        }

        stage('Build') {
            steps {
                mvnw('package -DskipTests')
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/lost-found.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
        }
        success {
            echo 'Pipeline succeeded: tests passed and lost-found.jar was archived.'
        }
        failure {
            echo 'Pipeline failed: check the Test or Build stage output above.'
        }
    }
}

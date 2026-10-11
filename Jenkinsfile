pipeline {
    agent any

    environment {
            REGISTRY_USER = 'resara101'
        IMAGE_NAME    = 'order-service'
        IMAGE_TAG     = "${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvn -B clean verify'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t %REGISTRY_USER%/%IMAGE_NAME%:%IMAGE_TAG% -t %REGISTRY_USER%/%IMAGE_NAME%:latest .'
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    bat '''
                        echo %DH_PASS%| docker login -u %DH_USER% --password-stdin
                        docker push %REGISTRY_USER%/%IMAGE_NAME%:%IMAGE_TAG%
                        docker push %REGISTRY_USER%/%IMAGE_NAME%:latest
                    '''
                }
            }
        }
    }

    post {
        always {
            bat 'docker logout'
            cleanWs()
        }
        success { echo "Pushed ${REGISTRY_USER}/${IMAGE_NAME}:${IMAGE_TAG}" }
        failure { echo 'Pipeline failed, check the stage logs above.' }
    }
}
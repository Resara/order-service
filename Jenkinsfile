pipeline {
    agent any

    environment {
        REGISTRY_USER = 'resara101'
        IMAGE_NAME    = 'order-service'
        IMAGE_TAG     = "${env.BUILD_NUMBER}"
        IMAGE_FULL    = "${REGISTRY_USER}/${IMAGE_NAME}:${BUILD_NUMBER}"
        HEALTH_URL    = 'http://localhost:8088/actuator/health'
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

        stage('Deploy') {
            steps {
                bat 'docker compose -p order-deploy -f docker-compose.deploy.yml pull order-service'
                bat 'docker compose -p order-deploy -f docker-compose.deploy.yml up -d'
            }
        }

        stage('Health Check') {
                steps {
                    script {
                        def healthy = false
                        for (int i = 1; i <= 24 && !healthy; i++) {
                            echo "Health check attempt ${i}/24"
                            def rc = bat(returnStatus: true,
                                         script: '@curl -s -f -o NUL %HEALTH_URL%')
                            if (rc == 0) {
                                healthy = true
                            } else {
                                sleep(time: 5, unit: 'SECONDS')
                            }
                        }
                        if (!healthy) {
                            error('Order Service did not become healthy within 2 minutes')
                        }
                        echo 'Order Service is healthy'
                    }
                }
            }
    }

    post {
        failure {
            echo 'Pipeline failed. Dumping container logs...'
            bat 'docker compose -p order-deploy -f docker-compose.deploy.yml logs --tail=100'
            bat 'docker compose -p order-deploy -f docker-compose.deploy.yml down -v'
        }
        success {
            echo "Deployed ${IMAGE_FULL} and health check passed"
        }
        always {
            bat 'docker logout'
            cleanWs()
        }
    }
}
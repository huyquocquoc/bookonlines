pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        // Docker Registry
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
        
        // Application versions
        APP_VERSION = "${env.BUILD_NUMBER}"
        
        // Service names
        AUTH_SERVICE = 'auth-service'
        BOOK_SERVICE = 'book-service'
        CART_SERVICE = 'cart-service'
        INVENTORY_SERVICE = 'inventory-service'
        NOTIFICATION_SERVICE = 'notification-service'
        
        // Docker image names
        AUTH_IMAGE = "${DOCKER_REGISTRY}/bookstore/${AUTH_SERVICE}"
        BOOK_IMAGE = "${DOCKER_REGISTRY}/bookstore/${BOOK_SERVICE}"
        CART_IMAGE = "${DOCKER_REGISTRY}/bookstore/${CART_SERVICE}"
        INVENTORY_IMAGE = "${DOCKER_REGISTRY}/bookstore/${INVENTORY_SERVICE}"
        NOTIFICATION_IMAGE = "${DOCKER_REGISTRY}/bookstore/${NOTIFICATION_SERVICE}"
        
        // SonarQube
        SONAR_HOST_URL = 'http://sonarqube:9000'
        SONAR_PROJECT_KEY = 'bookstore-microservices'
        
        // Deployment
        DEPLOY_ENV = "${env.BRANCH_NAME == 'main' ? 'production' : 'staging'}"
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out code from ${env.GIT_BRANCH}"
                    checkout scm
                }
            }
        }
        
        stage('Build Common Module') {
            steps {
                dir('backend/common') {
                    script {
                        echo 'Building common module...'
                        sh 'mvn clean install -DskipTests'
                    }
                }
            }
        }
        
        stage('Build Services') {
            parallel {
                stage('Build Auth Service') {
                    steps {
                        dir('backend/auth-service') {
                            script {
                                echo 'Building Auth Service...'
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
                
                stage('Build Book Service') {
                    steps {
                        dir('backend/book-service') {
                            script {
                                echo 'Building Book Service...'
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
                
                stage('Build Cart Service') {
                    steps {
                        dir('backend/cart-service') {
                            script {
                                echo 'Building Cart Service...'
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
                
                stage('Build Inventory Service') {
                    steps {
                        dir('backend/inventory-service') {
                            script {
                                echo 'Building Inventory Service...'
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
                
                stage('Build Notification Service') {
                    steps {
                        dir('backend/notification-service') {
                            script {
                                echo 'Building Notification Service...'
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Unit Tests') {
            parallel {
                stage('Test Auth Service') {
                    steps {
                        dir('backend/auth-service') {
                            script {
                                echo 'Running Auth Service tests...'
                                sh 'mvn test'
                            }
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                
                stage('Test Book Service') {
                    steps {
                        dir('backend/book-service') {
                            script {
                                echo 'Running Book Service tests...'
                                sh 'mvn test'
                            }
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
                
                stage('Test Cart Service') {
                    steps {
                        dir('backend/cart-service') {
                            script {
                                echo 'Running Cart Service tests...'
                                sh 'mvn test'
                            }
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }
        
        stage('Code Quality Analysis') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo 'Running SonarQube analysis...'
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            cd backend
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.host.url=${SONAR_HOST_URL}
                        '''
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo 'Waiting for Quality Gate...'
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Build Auth Image') {
                    steps {
                        dir('backend/auth-service') {
                            script {
                                echo "Building Docker image for Auth Service..."
                                sh """
                                    docker build -t ${AUTH_IMAGE}:${APP_VERSION} \
                                                 -t ${AUTH_IMAGE}:latest .
                                """
                            }
                        }
                    }
                }
                
                stage('Build Book Image') {
                    steps {
                        dir('backend/book-service') {
                            script {
                                echo "Building Docker image for Book Service..."
                                sh """
                                    docker build -t ${BOOK_IMAGE}:${APP_VERSION} \
                                                 -t ${BOOK_IMAGE}:latest .
                                """
                            }
                        }
                    }
                }
                
                stage('Build Cart Image') {
                    steps {
                        dir('backend/cart-service') {
                            script {
                                echo "Building Docker image for Cart Service..."
                                sh """
                                    docker build -t ${CART_IMAGE}:${APP_VERSION} \
                                                 -t ${CART_IMAGE}:latest .
                                """
                            }
                        }
                    }
                }
                
                stage('Build Inventory Image') {
                    steps {
                        dir('backend/inventory-service') {
                            script {
                                echo "Building Docker image for Inventory Service..."
                                sh """
                                    docker build -t ${INVENTORY_IMAGE}:${APP_VERSION} \
                                                 -t ${INVENTORY_IMAGE}:latest .
                                """
                            }
                        }
                    }
                }
                
                stage('Build Notification Image') {
                    steps {
                        dir('backend/notification-service') {
                            script {
                                echo "Building Docker image for Notification Service..."
                                sh """
                                    docker build -t ${NOTIFICATION_IMAGE}:${APP_VERSION} \
                                                 -t ${NOTIFICATION_IMAGE}:latest .
                                """
                            }
                        }
                    }
                }
            }
        }
        
        stage('Security Scan') {
            parallel {
                stage('Scan Auth Image') {
                    steps {
                        script {
                            echo "Scanning Auth Service image for vulnerabilities..."
                            sh "docker scan ${AUTH_IMAGE}:${APP_VERSION} || true"
                        }
                    }
                }
                
                stage('Scan Book Image') {
                    steps {
                        script {
                            echo "Scanning Book Service image for vulnerabilities..."
                            sh "docker scan ${BOOK_IMAGE}:${APP_VERSION} || true"
                        }
                    }
                }
            }
        }
        
        stage('Push Docker Images') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    echo 'Pushing Docker images to registry...'
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        sh """
                            docker push ${AUTH_IMAGE}:${APP_VERSION}
                            docker push ${AUTH_IMAGE}:latest
                            
                            docker push ${BOOK_IMAGE}:${APP_VERSION}
                            docker push ${BOOK_IMAGE}:latest
                            
                            docker push ${CART_IMAGE}:${APP_VERSION}
                            docker push ${CART_IMAGE}:latest
                            
                            docker push ${INVENTORY_IMAGE}:${APP_VERSION}
                            docker push ${INVENTORY_IMAGE}:latest
                            
                            docker push ${NOTIFICATION_IMAGE}:${APP_VERSION}
                            docker push ${NOTIFICATION_IMAGE}:latest
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    echo 'Deploying to Staging environment...'
                    sh '''
                        chmod +x ./scripts/deploy-staging.sh
                        ./scripts/deploy-staging.sh ${APP_VERSION}
                    '''
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo 'Deploying to Production environment...'
                    input message: 'Deploy to Production?', ok: 'Deploy'
                    
                    sh '''
                        chmod +x ./scripts/deploy-production.sh
                        ./scripts/deploy-production.sh ${APP_VERSION}
                    '''
                }
            }
        }
        
        stage('Health Check') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    echo 'Running health checks...'
                    sh '''
                        chmod +x ./scripts/health-check.sh
                        ./scripts/health-check.sh ${DEPLOY_ENV}
                    '''
                }
            }
        }
        
        stage('Smoke Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    echo 'Running smoke tests...'
                    sh '''
                        chmod +x ./scripts/smoke-tests.sh
                        ./scripts/smoke-tests.sh ${DEPLOY_ENV}
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            cleanWs()
        }
        
        success {
            echo 'Pipeline completed successfully!'
            emailext(
                subject: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """
                    <p>Build succeeded!</p>
                    <p>Job: ${env.JOB_NAME}</p>
                    <p>Build Number: ${env.BUILD_NUMBER}</p>
                    <p>Build URL: ${env.BUILD_URL}</p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }
        
        failure {
            echo 'Pipeline failed!'
            emailext(
                subject: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """
                    <p>Build failed!</p>
                    <p>Job: ${env.JOB_NAME}</p>
                    <p>Build Number: ${env.BUILD_NUMBER}</p>
                    <p>Build URL: ${env.BUILD_URL}</p>
                    <p>Please check the console output for details.</p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }
        
        unstable {
            echo 'Pipeline is unstable!'
        }
    }
} // end of pipeline

// Made with Bob

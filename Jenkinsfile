pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['staging', 'production', 'local'], description: 'Target Deployment Environment')
        string(name: 'SERVER_PORT', defaultValue: '8080', description: 'Application Port')
        booleanParam(name: 'AUTO_START_SERVER', defaultValue: true, description: 'Trigger server lifecycle on target node')
    }

    environment {
        APP_NAME = 'fitness-portal'
        VERSION = '1.0.0-mvp'
        DEPLOY_DIR = "deployments/${params.DEPLOY_ENV}"
        HEALTH_ENDPOINT = "http://localhost:${params.SERVER_PORT}/api/health"
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout SCM') {
            steps {
                echo "=========================================================="
                echo "Stage 1: Checking out branch from SCM..."
                echo "Repository: https://github.com/manthanrane68-cpu/fitness-equipment-maintenance-portal.git"
                echo "Target Environment: ${params.DEPLOY_ENV}"
                echo "Target Port: ${params.SERVER_PORT}"
                echo "=========================================================="
                checkout scm
            }
        }

        stage('Build & Compile') {
            steps {
                echo "Stage 2: Compiling Java source with Maven 3.9..."
                bat 'mvn clean compile'
            }
        }

        stage('Automated Quality Tests') {
            steps {
                echo "Stage 3: Running Automated Test Suites..."
                bat 'mvn test -Dtest=FitnessPortalServerTest'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package Application') {
            steps {
                echo "Stage 4: Packaging standalone fat JAR / WAR..."
                bat 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy to Server') {
            steps {
                echo "Stage 5: Deploying ${APP_NAME}.jar to environment [${params.DEPLOY_ENV}] on port [${params.SERVER_PORT}]..."
                script {
                    if (isUnix()) {
                        sh "./jenkins/deploy.sh ${params.DEPLOY_ENV} ${params.SERVER_PORT}"
                    } else {
                        bat "powershell -ExecutionPolicy Bypass -File jenkins/deploy.ps1 -Environment ${params.DEPLOY_ENV} -Port ${params.SERVER_PORT}"
                    }
                }
            }
        }

        stage('Post-Deployment Health Gate') {
            steps {
                echo "Stage 6: Performing Automated Health Diagnostics on ${env.HEALTH_ENDPOINT}..."
                script {
                    if (isUnix()) {
                        sh "curl -f -s ${env.HEALTH_ENDPOINT} || exit 1"
                    } else {
                        bat "powershell -ExecutionPolicy Bypass -Command \"Invoke-RestMethod -Uri '${env.HEALTH_ENDPOINT}'\""
                    }
                }
            }
        }
    }

    post {
        success {
            echo "SUCCESS: Pipeline completed successfully! Application is live on port ${params.SERVER_PORT}"
        }
        failure {
            echo "CRITICAL: Pipeline failed. Check console output and test reports."
        }
    }
}

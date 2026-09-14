pipeline {
    agent any

    tools {
        maven 'MAVEN3'
    }

    environment {
        // Variables for easy configuration
        DOCKER_CREDS_ID = 'dockerhub-creds'
        EC2_CREDS_ID = 'ec2-ssh-keys'
        EC2_IP = '16.112.208.192'
        EC2_USER = 'ubuntu'
        K8S_MANIFEST_DIR = 'k8s' // Directory in your repo containing frontend.yml, backend.yml, database.yml
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
                echo 'Source code checked out successfully from GitHub.'
            }
        }

        stage('Build') {
            steps {
                dir('backend') {
                    sh 'mvn clean compile'
                    echo 'Backend compiled successfully.'
                }
            }
        }

        stage('Test') {
            steps {
                dir('backend') {
                    sh 'mvn test'
                    echo 'Automated tests passed successfully.'
                }
            }
        }

        stage('Package') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests' // Skip tests here since we already ran them in the Test stage
                }
            }
        }

        stage('Docker Build') {
            steps {
                // Builds images based on your docker-compose.yml or individual Dockerfiles
                sh 'docker compose build'
                echo 'Docker images built locally.'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: env.DOCKER_CREDS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    // Push the newly built images to Docker Hub so Kubernetes can pull them
                    sh 'docker compose push'
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sshagent([env.EC2_CREDS_ID]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} 'mkdir -p ~/deployment'

                        scp -o StrictHostKeyChecking=no -r ${env.K8S_MANIFEST_DIR} ${env.EC2_USER}@${env.EC2_IP}:~/deployment/
                        scp -o StrictHostKeyChecking=no -r monitoring ${env.EC2_USER}@${env.EC2_IP}:~/deployment/

                        ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} '
                            cd ~/deployment &&
                            echo "Deploying monitoring stack (Prometheus + Grafana)..." &&
                            kubectl apply -f monitoring/ &&
                            echo "Applying application manifests..." &&
                            kubectl apply -f ${env.K8S_MANIFEST_DIR}/ &&
                            echo "Restarting deployments for zero-downtime update..." &&
                            kubectl rollout restart deployment/frontend deployment/backend || true
                        '
                    """
                }
            }
        }
    }    
    
    post {
        always {
            echo 'Pipeline execution completed.'
        }
        success {
            echo 'Kubernetes deployment successful!'
        }
        failure {
            echo 'Deployment failed. Check console logs for details.'
        }
    }
}

pipeline {
    agent any

    tools {
        maven 'MAVEN3'
    }

    environment {
        // Variables for easy configuration
        DOCKER_CREDS_ID = 'dockerhub-creds'
        EC2_CREDS_ID = 'ec2-ssh-keys'
        EC2_IP = '44.219.16.192'
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
                // Use the SSH agent plugin to connect to the EC2 instance hosting the K8s cluster
                sshagent([env.EC2_CREDS_ID]) {
                    sh """
                        # 1. Create a deployment directory on the remote EC2 server
                        ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} 'mkdir -p ~/deployment'

                        # 2. Securely copy the entire k8s manifests directory to the remote server
                        scp -o StrictHostKeyChecking=no -r ${env.K8S_MANIFEST_DIR} ${env.EC2_USER}@${env.EC2_IP}:~/deployment/

                        # 3. SSH into the remote server, apply Kubernetes manifests, and restart deployments
                        ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} '
                            cd ~/deployment &&
                            echo "Applying Kubernetes manifests..." &&
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
            echo '✅ Kubernetes deployment successful!'
        }
        failure {
            echo '❌ Deployment failed. Check console logs for details.'
        }
    }
}

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
    }
    
    stages {
        stage('Checkout Code') {
            steps {
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
                    sh 'mvn package'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                // Docker compose build will use the tags from docker-compose.yml
                sh 'docker compose build'
                echo 'Docker images built locally.'
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                // Log in to Docker Hub using the credentials stored in Jenkins
                withCredentials([usernamePassword(credentialsId: env.DOCKER_CREDS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    
                    // Push the newly built images to Docker Hub
                    sh 'docker compose push'
                }
            }
        }
        
        stage('Deploy to Remote EC2') {
            steps {
                // Use the SSH agent plugin with the EC2 private key
                sshagent([env.EC2_CREDS_ID]) {
                    // 1. Create a directory on the remote server
                    sh "ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} 'mkdir -p ~/deployment'"
                    
                    // 2. Securely copy the docker-compose.yml to the new EC2 server
                    sh "scp -o StrictHostKeyChecking=no docker-compose.yml ${env.EC2_USER}@${env.EC2_IP}:~/deployment/"
                    
                    // 3. SSH into the remote server, pull the images, and start the app
                    sh """
                        ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} '
                            cd ~/deployment &&
                            docker compose pull &&
                            docker compose down &&
                            docker compose up -d
                        '
                    """
                }
            }
        }
    }
}

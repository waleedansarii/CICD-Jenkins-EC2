pipeline {
    agent any

    environment {
        DOCKER_CREDS_ID  = 'dockerhub-creds'
        EC2_CREDS_ID     = 'ec2-ssh-keys'
        EC2_IP           = '16.112.208.192'
        EC2_USER         = 'ubuntu'
        K8S_MANIFEST_DIR = 'k8s'
    }

    stages {
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
            cleanWs()
        }
        success {
            echo '✅ Deployment completed successfully!'
        }
        failure {
            echo '❌ Deployment failed. Check console logs.'
        }
    }
}

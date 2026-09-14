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

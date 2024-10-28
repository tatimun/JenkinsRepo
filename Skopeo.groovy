pipeline {
    agent any

    environment {
        // URL del registro de imágenes en OpenShift
        OPENSHIFT_REGISTRY = 'docker://default-route-openshift-image-registry.apps-crc.testing'
        IMAGE_NAME = 'hello-world'
        IMAGE_TAG = '1'
        QUAY_REPO = 'quay.io/tatisinc/hello-world'
    }

    stages {
        stage('Login to OpenShift') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'jenkins-sa-token', variable: 'OC_TOKEN')]) {
                        sh "oc login --token=${OC_TOKEN} --server=https://api-route-default.apps-crc.testing --insecure-skip-tls-verify"
                    }
                }
            }
        }

        stage('Login to Quay.io with Skopeo') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'quay-credential', usernameVariable: 'QUAY_USERNAME', passwordVariable: 'QUAY_PASSWORD')]) {
                        sh "echo '${QUAY_PASSWORD}' | skopeo login quay.io -u '${QUAY_USERNAME}' --password-stdin"
                    }
                }
            }
        }

        stage('Copy Image from OpenShift to Quay.io') {
            steps {
                script {
                    sh """
                    skopeo copy --src-tls-verify=false --dest-tls-verify=false \
                      ${OPENSHIFT_REGISTRY}/default/${IMAGE_NAME}:${IMAGE_TAG} \
                      docker://${QUAY_REPO}:${IMAGE_TAG}
                    """
                }
            }
        }
    }

    post {
        always {
            sh "oc logout"
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Please check the logs for more details.'
        }
    }
}

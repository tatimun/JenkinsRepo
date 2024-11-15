pipeline {
    agent any

    environment {
        GITLAB_URL = 'https://gitlab.com/tatimunoz/scripts.git' 
        GITLAB_CREDENTIALS = 'gitlab-credentials' 
        AZURE_CREDENTIALS = 'azure-credentials'
        AZURE_STORAGE_ACCOUNT = 'storagetatitest' 
        AZURE_CONTAINER_NAME = 'test' 
        FILE_PATH = "${WORKSPACE}" 
        FILE_NAME = 'SharePointOnlineManagementShell_25409-12000_en-us.msi' 
    }

    triggers {
        cron('H * * * *') 
    }

    stages {
        stage('Clonar Repositorio de GitLab') {
            steps {
                git branch: 'main', credentialsId: "${GITLAB_CREDENTIALS}", url: "${GITLAB_URL}"
            }
        }

        stage('Ejecutar Script de Selenium') {
            steps {
                bat 'pip install -r requirements.txt'
                bat 'python script.py'
            }
        }

        stage('Verificar Archivo Descargado') {
            steps {
                // Lista los archivos en el directorio de trabajo para verificar si el archivo descargado está presente
                bat 'dir /B'
            }
        }

        stage('Autenticación en Azure') {
            steps {
                withCredentials([azureServicePrincipal(
                    credentialsId: "${AZURE_CREDENTIALS}",
                    subscriptionIdVariable: 'AZURE_SUBSCRIPTION_ID',
                    clientIdVariable: 'AZURE_CLIENT_ID',
                    clientSecretVariable: 'AZURE_CLIENT_SECRET',
                    tenantIdVariable: 'AZURE_TENANT_ID'
                )]) {
                    bat '''
                    az login --service-principal -u %AZURE_CLIENT_ID% -p %AZURE_CLIENT_SECRET% --tenant %AZURE_TENANT_ID%
                    az account set --subscription %AZURE_SUBSCRIPTION_ID%
                    '''
                }
            }
        }

        stage('Subir a Azure Storage') {
            steps {
                script {
                    if (fileExists("${FILE_PATH}\\${FILE_NAME}")) {
                        def timestamp = new Date().format("yyyyMMdd-HHmmss") // Incluye hora, minutos y segundos
                        def uniqueFileName = "${FILE_NAME}_${timestamp}" // Agregar timestamp al nombre

                        bat """
                        az storage blob upload --account-name ${AZURE_STORAGE_ACCOUNT} --container-name ${AZURE_CONTAINER_NAME} --file ${FILE_PATH}\\${FILE_NAME} --name ${uniqueFileName}
                        """
                    } else {
                        error("El archivo ${FILE_PATH}\\${FILE_NAME} no existe.")
                    }
                }
            }
        }
    }

    post {
        always {
            bat 'az logout'
        }
    }
}

This jenkinsfile requires:

- Azure Credentials which require App registration, Secret, AppID, TenantID, Subscription ID (Azure Credentials Plugin)
- Storage Account
- App registration should have permission over the Storage Account
- Gitlab Credentials since it was configured with GitLab

It runs for Windows node


Script: Downloads Sharepoint Online Management using google chrome and google driver with selenium
Jenkinsfile: Schedule the script, and uploads the tool to storage account
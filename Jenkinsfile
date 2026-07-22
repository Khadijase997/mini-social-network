pipeline {
    agent any

    environment {
        DTRACK_URL = 'http://dtrack-apiserver:8080'
        DTRACK_API_KEY = credentials('dtrack-api-key')
        PROJECT_NAME = 'mini-social-network'
        PROJECT_VERSION = "${env.BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Gitleaks - Secrets Detection') {
            steps {
                sh 'gitleaks detect --source . --report-format json --report-path gitleaks-report.json --exit-code 0'
            }
        }

        stage('Semgrep - SAST') {
            steps {
                sh 'semgrep scan --config auto --json --output semgrep-report.json .'
            }
        }

        stage('Build Jar') {
            steps {
                sh './gradlew build -x test'
            }
        }

        stage('Syft - SBOM Generation') {
            steps {
                sh 'syft dir:. -o cyclonedx-json=sbom.json'
            }
        }

        stage('Grype - SCA') {
            steps {
                sh 'grype sbom:./sbom.json -o json > grype-report.json'
            }
        }

        stage('Publish SBOM to Dependency-Track') {
            steps {
                sh """
                    curl -X POST "${DTRACK_URL}/api/v1/bom" \
                    -H "X-Api-Key: ${DTRACK_API_KEY}" \
                    -H "Content-Type: multipart/form-data" \
                    -F "autoCreate=true" \
                    -F "projectName=${PROJECT_NAME}" \
                    -F "projectVersion=${PROJECT_VERSION}" \
                    -F "bom=@sbom.json"
                """
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'gitleaks-report.json, semgrep-report.json, sbom.json, grype-report.json', allowEmptyArchive: true
        }
    }
}
pipeline {
    agent any

    environment {
        DTRACK_URL = 'http://dtrack-apiserver:8080'
        DTRACK_URL = 'http://dependency-track:8080'
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
                sh '''
                    docker run --rm -v $(pwd):/repo zricethezav/gitleaks:latest \
                    detect --source /repo --report-format json --report-path /repo/gitleaks-report.json --exit-code 0
                '''
            }
        }

        stage('Semgrep - SAST') {
            steps {
                sh '''
                    docker run --rm -v $(pwd):/src returntocorp/semgrep:latest \
                    semgrep scan --config auto --json --output /src/semgrep-report.json /src
                '''
            }
        }

      stage('Build Jar') {
    steps {
        sh 'chmod +x gradlew'
        sh './gradlew build -x test'
    }
}

        stage('Syft - SBOM Generation') {
            steps {
                sh '''
                    docker run --rm -v $(pwd):/src anchore/syft:latest \
                    dir:/src -o cyclonedx-json=/src/sbom.json
                '''
            }
        }

        stage('Grype - SCA') {
            steps {
                sh '''
                    docker run --rm -v $(pwd):/src anchore/grype:latest \
                    sbom:/src/sbom.json -o json > grype-report.json
                '''
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
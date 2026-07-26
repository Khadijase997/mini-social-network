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
        sh 'chmod +x ./gradlew'
        sh './gradlew build -x test'
    }
}

        stage('Syft - SBOM Generation') {
    steps {
        sh 'syft dir:. -o cyclonedx-json@1.5=sbom.json'
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
stage('Policy Gate Check') {          // ← à ajouter ici
    steps {
        script {
            def response = sh(
                script: '''
                    curl -s -X GET "http://dtrack-apiserver:8080/api/v1/violation/project/${PROJECT_UUID}" \
                    -H "X-Api-Key: ${DTRACK_API_KEY}"
                ''',
                returnStdout: true
            ).trim()

            def violations = readJSON text: response
            def blocking = violations.findAll { it.policyCondition.policy.name == 'sentrix-policy-gate' && it.type == 'FAIL' }

            if (blocking.size() > 0) {
                error("Build bloqué : vulnérabilité(s) critique(s) détectée(s) via Policy Gate")
            }
        }
    }
}

    }

    post {
        always {
            archiveArtifacts artifacts: 'gitleaks-report.json, semgrep-report.json, sbom.json, grype-report.json', allowEmptyArchive: true
        }
    }
}
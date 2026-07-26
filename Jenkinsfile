pipeline {
    agent any
    environment {
        DTRACK_URL = 'http://dtrack-apiserver:8080'
        DTRACK_API_KEY = credentials('dtrack-api-key')
        PROJECT_NAME = 'mini-social-network'
        PROJECT_VERSION = 'latest'
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
                script {
                    def uploadResponse = sh(
                        script: '''
                            curl -s -X POST "${DTRACK_URL}/api/v1/bom" \
                            -H "X-Api-Key: ${DTRACK_API_KEY}" \
                            -H "Content-Type: multipart/form-data" \
                            -F "autoCreate=true" \
                            -F "projectName=${PROJECT_NAME}" \
                            -F "projectVersion=${PROJECT_VERSION}" \
                            -F "bom=@sbom.json"
                        ''',
                        returnStdout: true
                    ).trim()

                    def upload = readJSON text: uploadResponse
                    env.BOM_TOKEN = upload.token
                    echo "Token de traitement BOM : ${env.BOM_TOKEN}"
                }
            }
        }
        stage('Wait for BOM Processing') {
            steps {
                script {
                    def maxAttempts = 20
                    def waitSeconds = 10
                    def processed = false

                    for (int i = 0; i < maxAttempts; i++) {
                        def statusResponse = sh(
                            script: '''
                                curl -s -X GET "${DTRACK_URL}/api/v1/bom/token/${BOM_TOKEN}" \
                                -H "X-Api-Key: ${DTRACK_API_KEY}"
                            ''',
                            returnStdout: true
                        ).trim()

                        def status = readJSON text: statusResponse
                        echo "Tentative ${i+1}/${maxAttempts} : processing = ${status.processing}"

                        if (status.processing == false) {
                            processed = true
                            break
                        }
                        sleep(time: waitSeconds, unit: 'SECONDS')
                    }

                    if (!processed) {
                        error("Timeout : Dependency-Track n'a pas terminé le traitement du BOM après ${maxAttempts * waitSeconds}s.")
                    }
                }
            }
        }
        stage('Fetch Project UUID') {
            steps {
                script {
                    def lookupResponse = sh(
                        script: '''
                            curl -s -X GET "${DTRACK_URL}/api/v1/project/lookup?name=${PROJECT_NAME}&version=${PROJECT_VERSION}" \
                            -H "X-Api-Key: ${DTRACK_API_KEY}"
                        ''',
                        returnStdout: true
                    ).trim()
                    def project = readJSON text: lookupResponse
                    env.PROJECT_UUID = project.uuid
                    echo "UUID récupéré : ${env.PROJECT_UUID}"
                }
            }
        }
        stage('Policy Gate Check') {
            steps {
                script {
                    def response = sh(
                        script: '''
                            curl -s -X GET "${DTRACK_URL}/api/v1/violation/project/${PROJECT_UUID}" \
                            -H "X-Api-Key: ${DTRACK_API_KEY}"
                        ''',
                        returnStdout: true
                    ).trim()

                    def violations = readJSON text: response
                    def blocking = violations.findAll {
                        it.policyCondition.policy.name == 'sentrix-policy-gate-blocking' && it.type == 'FAIL'
                    }

                    echo "Violations bloquantes détectées : ${blocking.size()}"

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
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Secrets Scan - Gitleaks') {
            steps {
                sh '''
                docker run --rm --volumes-from jenkins -w "$(pwd)" zricethezav/gitleaks detect \
                  --source . --report-format json --report-path gitleaks-report.json --exit-code 0
                '''
                archiveArtifacts artifacts: 'gitleaks-report.json'
            }
        }
        stage('SAST - Semgrep') {
            steps {
                sh '''
                docker run --rm --volumes-from jenkins -w "$(pwd)" returntocorp/semgrep semgrep scan \
                  --config=auto . --json --output=semgrep-report.json
                '''
                archiveArtifacts artifacts: 'semgrep-report.json'
            }
        }
                stage('SBOM - Syft') {
            steps {
                sh '''
                docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  anchore/syft mini-social-network:${GIT_COMMIT} -o cyclonedx-json@1.5 > sbom.json
                '''
                archiveArtifacts artifacts: 'sbom.json'
            }
        }
        stage('SCA - Grype') {
            steps {
                sh '''
                docker run --rm --volumes-from jenkins -w "$(pwd)" anchore/grype sbom:sbom.json \
                  -o json > scan-results.json
                '''
                archiveArtifacts artifacts: 'scan-results.json'
            }
        }
stage('Install Dependencies') {
    steps {
        sh '''
        npm ci --prefix .
        npm ci --prefix frontend
        '''
    }
}
        stage('Publish to Dependency-Track') {
            steps {
                withCredentials([string(credentialsId: 'dtrack-api-key', variable: 'DTRACK_KEY')]) {
                    sh '''
                    curl -sS -w "\\nHTTP_STATUS:%{http_code}\\n" -X POST "http://host.docker.internal:8091/api/v1/bom" \
                      -H "X-Api-Key: $DTRACK_KEY" \
                      -F "projectName=mini-social-network" \
                      -F "projectVersion=1.0" \
                      -F "autoCreate=true" \
                      -F "bom=@sbom.json"
                    '''
                }
            }
        }
stage('Build image') {
            steps {
                sh 'docker build -t mini-social-network:${GIT_COMMIT} .'
            }
        }


        stage('Deploy Neo4j') {
            steps {
                sh '''
                docker network rm sentrix-net 2>/dev/null || true
                docker network create sentrix-net || true

                docker rm -f neo4j-target || true
                docker run -d --name neo4j-target --network sentrix-net \
                  -e NEO4J_AUTH=neo4j/testpassword \
                  neo4j:latest

                echo "Attente du démarrage de Neo4j..."
                for i in $(seq 1 30); do
                  if docker exec neo4j-target cypher-shell -u neo4j -p testpassword "RETURN 1" > /dev/null 2>&1; then
                    echo "Neo4j prêt."
                    break
                  fi
                  echo "En attente de Neo4j..."
                  sleep 2
                done
                '''
            }
        }

        stage('Deploy DAST target') {
            steps {
                sh '''
                docker rm -f dast-target || true
                docker run -d --name dast-target --network sentrix-net -p 8080:8080 \
                  -e SPRING_NEO4J_URI=bolt://neo4j-target:7687 \
                  -e SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j \
                  -e SPRING_NEO4J_AUTHENTICATION_PASSWORD=testpassword \
                  mini-social-network:${GIT_COMMIT}
                '''
            }
        }
        stage('Wait for target readiness') {
            steps {
                sh '''
                echo "Attente du démarrage de l'application..."
                for i in $(seq 1 30); do
                  if curl -sf http://host.docker.internal:8080 > /dev/null; then
                    echo "Application prête."
                    break
                  fi
                  echo "En attente..."
                  sleep 2
                done
                '''
            }
        }
        stage('DAST - OWASP ZAP') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh '''
                    docker volume rm zap-wrk || true
                    docker volume create zap-wrk

                    docker run --rm --user root -v zap-wrk:/zap/wrk \
                      zaproxy/zap-stable chmod -R 777 /zap/wrk

                    docker rm -f zap-run || true
                    docker create --name zap-run -v zap-wrk:/zap/wrk \
                      zaproxy/zap-stable zap-baseline.py \
                      -t http://host.docker.internal:8080 \
                      -r zap-report.html \
                      -J zap-report.json \
                      -I

                    docker start -a zap-run || true
                    docker cp zap-run:/zap/wrk/zap-report.html . || true
                    docker cp zap-run:/zap/wrk/zap-report.json . || true

                    docker rm -f zap-run || true
                    docker volume rm zap-wrk || true
                    '''
                }
                archiveArtifacts artifacts: 'zap-report.html, zap-report.json', allowEmptyArchive: true
                publishHTML(target: [
                    reportDir: '.',
                    reportFiles: 'zap-report.html',
                    reportName: 'ZAP DAST Report',
                    keepAll: true
                ])
            }
        }
        stage('Cleanup DAST target') {
            steps {
                sh '''
                docker rm -f dast-target || true
                docker rm -f neo4j-target || true
                docker network rm sentrix-net || true
                '''
            }
        }
    }
}
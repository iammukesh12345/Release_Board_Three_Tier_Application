pipeline {
    agent any

    tools {
        maven 'maven3'   // Manage Jenkins > Tools
        jdk   'jdk17'
    }

    triggers {
        githubPush()      // fires on the GitHub webhook (payload URL: http://<jenkins>:8080/github-webhook/)
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        NEXUS_URL      = 'http://<NEXUS_HOST>:8081'     // change me
        DOCKERHUB_USER = '<your-dockerhub-username>'    // change me
        IMAGE_TAG      = "${env.BUILD_NUMBER}"
        BACKEND_IMG    = "${DOCKERHUB_USER}/releaseboard-backend"
        FRONTEND_IMG   = "${DOCKERHUB_USER}/releaseboard-frontend"
        DB_IMG         = "${DOCKERHUB_USER}/releaseboard-database"
    }

    stages {

        stage('1. Clone source code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/<your-org>/<your-repo>.git',
                    credentialsId: 'github-creds'
            }
        }

        stage('2. Build with Maven') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    dir('backend') {
                        sh 'mvn -B -s settings.xml clean package -DskipTests'
                    }
                }
            }
        }

        stage('3. Unit tests') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    dir('backend') {
                        sh 'mvn -B -s settings.xml surefire:test'
                    }
                }
            }
            post { always { junit 'backend/target/surefire-reports/*.xml' } }
        }

        stage('4. OWASP Dependency-Check') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD'),
                    string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
                    dir('backend') {
                        sh 'mvn -B -s settings.xml org.owasp:dependency-check-maven:check'
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'backend/target/dependency-check-report.*', allowEmptyArchive: true
                }
            }
        }

        stage('5. Integration tests') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    dir('backend') {
                        sh 'mvn -B -s settings.xml failsafe:integration-test failsafe:verify'
                    }
                }
            }
            post { always { junit allowEmptyResults: true, testResults: 'backend/target/failsafe-reports/*.xml' } }
        }

        stage('6. Deploy artifact to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                    dir('backend') {
                        sh 'mvn -B -s settings.xml deploy -DskipTests'
                    }
                }
            }
        }

        stage('7. Build Docker images') {
            steps {
                sh '''
                  docker build -t $BACKEND_IMG:$IMAGE_TAG  -t $BACKEND_IMG:latest  backend
                  docker build -t $FRONTEND_IMG:$IMAGE_TAG -t $FRONTEND_IMG:latest frontend
                  docker build -t $DB_IMG:$IMAGE_TAG       -t $DB_IMG:latest       database
                '''
            }
        }

        stage('8. Trivy image scan') {
            steps {
                sh '''
                  for IMG in $BACKEND_IMG $FRONTEND_IMG $DB_IMG; do
                    # Full report (HIGH + CRITICAL), never fails the build
                    trivy image --no-progress --severity HIGH,CRITICAL --exit-code 0 $IMG:$IMAGE_TAG
                    # Gate: fail the build only on CRITICAL findings that have a fix available
                    trivy image --no-progress --severity CRITICAL --ignore-unfixed --exit-code 1 $IMG:$IMAGE_TAG
                  done
                '''
            }
        }

        stage('9. Push images to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                    sh '''
                      echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                      for IMG in $BACKEND_IMG $FRONTEND_IMG $DB_IMG; do
                        docker push $IMG:$IMAGE_TAG
                        docker push $IMG:latest
                      done
                    '''
                }
            }
        }
    }

    post {
        always { sh 'docker logout || true' }
        success { echo "Pipeline OK - images tagged ${IMAGE_TAG} pushed to Docker Hub" }
        failure { echo 'Pipeline failed - check the stage logs above' }
    }
}

pipeline {
    agent any

    environment {
        // =========================================================================
        // 1. CẤU HÌNH DOCKER HUB & PHIÊN BẢN IMAGE
        // =========================================================================
        DOCKER_HUB_USER     = 'phwkha'
        BACKEND_IMAGE_NAME  = "${DOCKER_HUB_USER}/confession-backend"
        FRONTEND_IMAGE_NAME = "${DOCKER_HUB_USER}/confession-frontend"
        IMAGE_TAG           = "${env.BUILD_NUMBER}"
        DOCKER_CREDS_ID     = 'docker-hub-credentials'
    }

    stages {
        stage('1. Checkout Code') {
            steps {
                echo '📥 [Step 1] Đang tải mã nguồn mới nhất từ GitHub...'
                checkout scm
            }
        }

        stage('2. Automated Testing') {
            steps {
                echo '🧪 [Step 2] Đang chạy bộ kiểm thử tự động (Unit Test Spring Boot)...'
                dir('backend') {
                    sh '''
                        if [ ! -f .mvn/wrapper/maven-wrapper.jar ]; then
                            echo "===> Đang tải maven-wrapper.jar tạm thời cho quá trình test..."
                            curl -sSL -o .mvn/wrapper/maven-wrapper.jar https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
                        fi
                        chmod +x mvnw
                        ./mvnw clean test
                    '''
                }
            }
        }

        stage('3. Build Docker Images') {
            steps {
                echo '🐳 [Step 3] Test passed! Đang đóng gói Docker Images cho Backend và Frontend...'
                script {
                    // Build Backend Image (Spring Boot 3)
                    sh """
                        echo "===> Building Backend Image..."
                        docker build -t ${BACKEND_IMAGE_NAME}:${IMAGE_TAG} \
                                     -t ${BACKEND_IMAGE_NAME}:latest \
                                     -f backend/Dockerfile backend/
                    """

                    // Build Frontend Image (React + Vite + Nginx)
                    sh """
                        echo "===> Building Frontend Image..."
                        docker build -t ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG} \
                                     -t ${FRONTEND_IMAGE_NAME}:latest \
                                     -f frontend/Dockerfile frontend/
                    """
                }
            }
        }

        stage('4. Push to Docker Hub') {
            steps {
                echo '🚀 [Step 4] Đang đăng nhập và đẩy Images lên Docker Hub...'
                script {
                    withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDS_ID}", 
                                                     usernameVariable: 'DH_USER', 
                                                     passwordVariable: 'DH_PASS')]) {
                        sh """
                            echo \$DH_PASS | docker login -u \$DH_USER --password-stdin
                            
                            echo "===> Pushing Backend Images..."
                            docker push ${BACKEND_IMAGE_NAME}:${IMAGE_TAG}
                            docker push ${BACKEND_IMAGE_NAME}:latest
                            
                            echo "===> Pushing Frontend Images..."
                            docker push ${FRONTEND_IMAGE_NAME}:${IMAGE_TAG}
                            docker push ${FRONTEND_IMAGE_NAME}:latest
                            
                            docker logout
                        """
                    }
                }
            }
        }

        stage('5. Deploy via SSH to Host') {
                steps {
                    echo "🚀 [Step 5] Đang kết nối SSH vào host để deploy bản #${IMAGE_TAG}..."
                    sshagent(credentials: ['deploy-server-ssh']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no phwkha@172.17.0.1 << 'EOF'
                                set -e
                                cd /home/phwkha/confession-wall
                                echo "===> Cập nhật biến môi trường phiên bản #${IMAGE_TAG}..."
                                export DOCKER_USERNAME=${DOCKER_HUB_USER}
                                export IMAGE_TAG=${IMAGE_TAG}
                                
                                echo "===> Kéo các images mới nhất từ Docker Hub..."
                                docker compose pull backend frontend
                                
                                echo "===> Khởi động lại Backend & Frontend với file .env chuẩn trên host..."
                                docker compose up -d --no-deps backend frontend
                                
                                echo "===> Kiểm tra trạng thái các container sau khi deploy:"
                                docker compose ps
                            EOF
                        """
                    }
                }
            }
    }

    post {
        always {
            echo '🧹 [Cleanup] Dọn dẹp các images dangling/trung gian để tránh đầy bộ nhớ...'
            sh 'docker image prune -f'
        }
        success {
            echo "🎉 [THÀNH CÔNG] Tất cả bài test đều PASS và bản build #${IMAGE_TAG} đã được deploy an toàn lên máy chủ!"
        }
        failure {
            echo "❌ [THẤT BẠI] Pipeline bị dừng do lỗi kiểm thử hoặc đóng gói! Môi trường Production vẫn an toàn."
        }
    }
}

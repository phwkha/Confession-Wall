pipeline {
    agent any

    triggers {
        githubPush()
    }

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
                echo 'Đang tải mã nguồn mới nhất từ GitHub...'
                checkout scm
            }
        }

        stage('2. Automated Testing') {
            steps {
                echo 'Đang chạy bộ kiểm thử tự động (Unit Test Spring Boot)...'
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
                echo 'Test passed! Đang đóng gói Docker Images cho Backend và Frontend...'
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
                echo 'Đang đăng nhập và đẩy Images lên Docker Hub...'
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
                echo "Đang kết nối SSH vào host để deploy bản #${IMAGE_TAG}..."
                sshagent(credentials: ['deploy-server-ssh']) {
                    sh """
                        scp -o StrictHostKeyChecking=accept-new docker-compose.yml phwkha@172.17.0.1:/home/phwkha/confession-wall/docker-compose.yml

                        ssh -o StrictHostKeyChecking=accept-new phwkha@172.17.0.1 << 'EOF'
                            set -e
                            cd /home/phwkha/confession-wall

                            if [ ! -f .env ]; then
                                echo "Không tìm thấy file /home/phwkha/confession-wall/.env trên host!"
                                exit 1
                            fi

                            echo "===> Cập nhật biến môi trường phiên bản #${IMAGE_TAG}..."
                            export DOCKER_USERNAME=${DOCKER_HUB_USER}
                            export IMAGE_TAG=${IMAGE_TAG}

                            echo "===> Kéo các images mới nhất từ Docker Hub..."
                            docker compose pull backend frontend

                            echo "===> Khởi động lại Backend & Frontend với file .env chuẩn trên host..."
                            docker compose up -d --no-deps backend frontend

                            echo "===> Kiểm tra trạng thái các container sau khi deploy:"
                            docker compose ps

                            echo "===> Đang kiểm tra sức khỏe ứng dụng (Health Check qua Nginx :80)..."
                            SUCCESS=false
                            for i in \$(seq 1 10); do
                                HTTP_CODE=\$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:80/api/confessions || echo "000")
                                if [ "\$HTTP_CODE" = "200" ]; then
                                    echo "API phản hồi 200 OK sau \${i} lần thử! Backend & CSDL đã sẵn sàng."
                                    SUCCESS=true
                                    break
                                fi
                                echo "   ...Chờ Backend khởi động và kết nối CSDL (lần \${i}/10, HTTP code: \$HTTP_CODE)..."
                                sleep 3
                            done

                            if [ "\$SUCCESS" != "true" ]; then
                                echo "Backend không phản hồi 200 OK sau 30 giây! Nhật ký lỗi Backend:"
                                docker logs confession_backend --tail 40
                                exit 1
                            fi
EOF
                    """
                }
            }
        }
    }

    post {
        always {
            echo 'Dọn dẹp các images dangling/trung gian'
            sh 'docker image prune -f'
        }
        success {
            echo "Tất cả bài test đều PASS và bản build #${IMAGE_TAG} đã được deploy "
        }
        failure {
            echo "Pipeline bị dừng do lỗi"
        }
    }
}

# Confession Wall (Bức Tường Thú Tội) — Fullstack Application

[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://adoptium.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF.svg)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3.4-38B2AC.svg)](https://tailwindcss.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

Ứng dụng fullstack "Bức Tường Thú Tội" (Confession Wall) hiện đại, cho phép người dùng chia sẻ tâm sự ẩn danh, duyệt bài viết theo thời gian thực và tương tác thả tim. Dự án được đóng gói container hoàn chỉnh với Docker & Docker Compose, sẵn sàng triển khai trên mọi môi trường máy chủ Linux / Cloud.

A modern fullstack "Confession Wall" application allowing anonymous confessions, real-time feed browsing, and interactive heart likes. The application is fully containerized with Docker & Docker Compose, ready for immediate production deployment.

---

## Mục Lục / Table of Contents
- [1. Kiến Trúc & Ngăn Xếp Công Nghệ (Architecture & Tech Stack)](#1-kiến-trúc--ngăn-xếp-công-nghệ-architecture--tech-stack)
- [2. Khởi Chạy Nhanh Với Docker Compose (Docker Quickstart)](#2-khởi-chạy-nhanh-với-docker-compose-docker-quickstart)
- [3. Hướng Dẫn Phát Triển Cục Bộ (Manual Local Development)](#3-hướng-dẫn-phát-triển-cục-bộ-manual-local-development)
- [4. Tài Liệu REST API & Ví Dụ cURL (REST API Documentation)](#4-tài-liệu-rest-api--ví-dụ-curl-rest-api-documentation)
- [5. Bộ Kiểm Thử E2E Tự Động (Automated E2E Test Suite)](#5-bộ-kiểm-thử-e2e-tự-động-automated-e2e-test-suite)
- [6. Cấu Trúc Mã Nguồn (Project Structure)](#6-cấu-trúc-mã-nguồn-project-structure)
- [7. English Documentation (Comprehensive Guide)](#7-english-documentation)

---

## 1. Kiến Trúc & Ngăn Xếp Công Nghệ (Architecture & Tech Stack)

### Sơ Đồ Kiến Trúc Hệ Thống (System Architecture Diagram)

```
                            +------------------------------------+
                            |          Trình Duyệt (Browser)     |
                            +-----------------+------------------+
                                              |
                             HTTP :3000       |        HTTP :8080 (hoặc qua /api reverse proxy)
                                              v
                            +-----------------+------------------+
                            |     Frontend Container (Nginx)     |
                            |       Port host 3000 -> Nginx :80  |
                            |       SPA Static Files + Proxy     |
                            +-----------------+------------------+
                                              |
                                              | proxy_pass http://backend:8080/api/
                                              v
                            +-----------------+------------------+
                            |   Backend Container (Spring Boot)  |
                            |       Port host 8080 -> App :8080  |
                            |       REST API, Validation, JPA    |
                            +-----------------+------------------+
                                              |
                                              | JDBC :5432 (pg_isready healthcheck)
                                              v
                            +-----------------+------------------+
                            |   Postgres Container (PostgreSQL)  |
                            |       Port host 5432 -> DB :5432   |
                            |       Named volume: postgres_data  |
                            +------------------------------------+
```

### Công Nghệ Sử Dụng (Technology Stack)
- **Backend**: Java 17, Spring Boot 3.2.4, Spring Data JPA, Hibernate, Bean Validation (`jakarta.validation`), PostgreSQL JDBC Driver.
- **Frontend**: React 18, Vite 5, Tailwind CSS 3.4, Axios, Lucide React icons.
- **Database**: PostgreSQL 15 (Alpine), lưu trữ bền vững qua Docker Named Volume `postgres_data`.
- **Web Server / Reverse Proxy**: Nginx Alpine (phục vụ SPA router và chuyển tiếp API nội bộ).
- **Containerization**: Docker Multi-stage Builds, Docker Compose v3.8+ với Healthchecks tự động.
- **Testing**: JUnit 5, Mockito, MockMvc (Backend Unit/Integration), Vitest (Frontend), Node.js Test Runner (E2E Blackbox Tiers 1-4).

---

## 2. Khởi Chạy Nhanh Với Docker Compose (Docker Quickstart)

### Yêu cầu tiên quyết (Prerequisites)
- Đã cài đặt [Docker Engine](https://docs.docker.com/engine/install/) (phiên bản 20.10 trở lên).
- Đã cài đặt [Docker Compose](https://docs.docker.com/compose/install/) (phiên bản v2 trở lên).

### Bước 1: Khởi động toàn bộ cụm dịch vụ (Build & Launch)
Chạy lệnh sau tại thư mục gốc của dự án:
```bash
docker compose up -d --build
```
Hệ thống sẽ tự động:
1. Tải image `postgres:15-alpine`, cấu hình cơ sở dữ liệu `confession_db` và khởi chạy kiểm tra sức khỏe (`pg_isready`).
2. Biên dịch mã nguồn Backend bằng Maven đa tầng (`maven:3.9.6-eclipse-temurin-17-alpine`) và tạo ảnh chạy nhẹ `eclipse-temurin:17-jre-alpine`.
3. Đợi container PostgreSQL chuyển sang trạng thái `healthy` trước khi khởi động Spring Boot.
4. Biên dịch mã nguồn Frontend React + Vite và đóng gói vào máy chủ Nginx nhẹ.
5. Thiết lập mạng cầu nối `confession-network` kết nối toàn bộ hệ thống.

### Bước 2: Kiểm tra trạng thái các dịch vụ (Check Service Status)
```bash
docker compose ps
```
**Kết quả mong đợi (Expected Output)**:
```
NAME                  IMAGE                  COMMAND                  SERVICE      STATUS                    PORTS
confession_backend    test-deploy-backend    "java -jar app.jar"      backend      running                   0.0.0.0:8080->8080/tcp
confession_frontend   test-deploy-frontend   "nginx -g 'daemon of…"   frontend     running                   0.0.0.0:3000->80/tcp
confession_postgres   postgres:15-alpine     "docker-entrypoint.s…"   postgres     running (healthy)         0.0.0.0:5432->5432/tcp
```

### Bước 3: Truy cập ứng dụng (Access Application)
- **Giao diện người dùng (Frontend SPA)**: [http://localhost:3000](http://localhost:3000)
- **Cổng REST API Backend**: [http://localhost:8080/api/confessions](http://localhost:8080/api/confessions)
- **Cơ sở dữ liệu PostgreSQL**: `localhost:5432` (User: `postgres`, Password: `postgres`, Database: `confession_db`)

### Bước 4: Xem nhật ký hoạt động (View Container Logs)
```bash
# Xem log toàn bộ hệ thống theo thời gian thực
docker compose logs -f

# Xem log riêng lẻ từng dịch vụ
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
```

### Bước 5: Tắt hoặc dọn dẹp hệ thống (Teardown & Clean)
```bash
# Dừng và gỡ bỏ containers, mạng
docker compose down

# Dừng và xóa luôn toàn bộ dữ liệu volume (Reset Database)
docker compose down -v
```

---

## 3. Hướng Dẫn Phát Triển Cục Bộ (Manual Local Development)

Nếu bạn muốn chạy ứng dụng trực tiếp trên máy không qua Docker Compose:

### Yêu cầu môi trường
- Java Development Kit (JDK) 17+
- Apache Maven 3.9+ (hoặc dùng `./mvnw` có sẵn)
- Node.js 18+ và npm 9+
- PostgreSQL 15+ đang chạy cục bộ tại cổng `5432`

### Bước 1: Chuẩn bị cơ sở dữ liệu PostgreSQL
Đăng nhập PostgreSQL và tạo cơ sở dữ liệu:
```sql
CREATE DATABASE confession_db;
CREATE USER postgres WITH ENCRYPTED PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE confession_db TO postgres;
```

### Bước 2: Chạy Backend (Spring Boot 3)
Di chuyển vào thư mục backend:
```bash
cd backend

# Khởi chạy ứng dụng với cấu hình mặc định (kết nối localhost:5432)
./mvnw spring-boot:run
```
Backend sẽ khởi động tại cổng `8080`. Bạn có thể chạy bộ kiểm thử đơn vị & tích hợp với:
```bash
./mvnw clean test
```

### Bước 3: Chạy Frontend (React + Vite)
Mở một cửa sổ terminal mới:
```bash
cd frontend

# Cài đặt thư viện phụ thuộc
npm install

# Khởi chạy máy chủ phát triển Vite với Hot Reload
npm run dev
```
Mở trình duyệt tại [http://localhost:3000](http://localhost:3000). Vite proxy sẽ tự động chuyển tiếp các yêu cầu `/api/*` tới `http://localhost:8080`.

---

## 4. Tài Liệu REST API & Ví Dụ cURL (REST API Documentation)

Tất cả các endpoints đều hỗ trợ CORS (`@CrossOrigin("*")`) và giao tiếp qua JSON định dạng UTF-8.

### 1. `GET /api/confessions`
Lấy danh sách toàn bộ bài thú tội, sắp xếp theo thời gian mới nhất lên đầu (`createdAt` DESC).

- **Phương thức**: `GET`
- **Đường dẫn**: `/api/confessions`
- **Tham số**: Không có
- **Mã phản hồi**: `200 OK`

**Ví dụ lệnh cURL**:
```bash
curl -X GET "http://localhost:8080/api/confessions" -H "Accept: application/json"
```

**Mẫu kết quả JSON (200 OK)**:
```json
[
  {
    "id": 1,
    "content": "Thích thầm bạn cùng bàn từ năm nhất mà không dám nói...",
    "author": "Ẩn danh",
    "likes": 5,
    "createdAt": "2026-09-13T10:15:30"
  }
]
```

---

### 2. `POST /api/confessions`
Tạo một bài thú tội mới. Nếu để trống hoặc không truyền tên tác giả, hệ thống tự động gán là `"Ẩn danh"`. Nội dung (`content`) không được để trống hoặc chỉ chứa khoảng trắng.

- **Phương thức**: `POST`
- **Đường dẫn**: `/api/confessions`
- **Header**: `Content-Type: application/json`
- **Mã phản hồi**: `201 Created` (hoặc `400 Bad Request` nếu dữ liệu không hợp lệ)

**Ví dụ 1: Gửi thành công với tác giả cụ thể**:
```bash
curl -X POST "http://localhost:8080/api/confessions" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Cảm ơn em vì đã xuất hiện trong thanh xuân của anh!",
    "author": "Minh Khang"
  }'
```
**Phản hồi (201 Created)**:
```json
{
  "id": 2,
  "content": "Cảm ơn em vì đã xuất hiện trong thanh xuân của anh!",
  "author": "Minh Khang",
  "likes": 0,
  "createdAt": "2026-09-13T10:20:00"
}
```

**Ví dụ 2: Gửi thành công ẩn danh (không truyền trường author)**:
```bash
curl -X POST "http://localhost:8080/api/confessions" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hôm nay trời đẹp quá, mong người tôi thương luôn bình an."
  }'
```
**Phản hồi (201 Created)**:
```json
{
  "id": 3,
  "content": "Hôm nay trời đẹp quá, mong người tôi thương luôn bình an.",
  "author": "Ẩn danh",
  "likes": 0,
  "createdAt": "2026-09-13T10:22:15"
}
```

**Ví dụ 3: Gửi thất bại do nội dung rỗng (Validation Failure)**:
```bash
curl -X POST "http://localhost:8080/api/confessions" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "   ",
    "author": "Test"
  }'
```
**Phản hồi lỗi (400 Bad Request)**:
```json
{
  "timestamp": "2026-09-13T10:25:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Nội dung lời thú tội không được để trống",
  "path": "/api/confessions"
}
```

---

### 3. `PUT /api/confessions/{id}/like`
Tăng số lượt thích (thả tim) của bài viết có ID tương ứng thêm 1 đơn vị.

- **Phương thức**: `PUT`
- **Đường dẫn**: `/api/confessions/{id}/like`
- **Mã phản hồi**: `200 OK` (hoặc `404 Not Found` nếu bài viết không tồn tại)

**Ví dụ 1: Thả tim thành công**:
```bash
curl -X PUT "http://localhost:8080/api/confessions/2/like"
```
**Phản hồi (200 OK)**:
```json
{
  "id": 2,
  "content": "Cảm ơn em vì đã xuất hiện trong thanh xuân của anh!",
  "author": "Minh Khang",
  "likes": 1,
  "createdAt": "2026-09-13T10:20:00"
}
```

**Ví dụ 2: Thả tim bài viết không tồn tại**:
```bash
curl -X PUT "http://localhost:8080/api/confessions/999999/like"
```
**Phản hồi lỗi (404 Not Found)**:
```json
{
  "timestamp": "2026-09-13T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Lời thú tội không tồn tại với ID: 999999",
  "path": "/api/confessions/999999/like"
}
```

---

## 5. Bộ Kiểm Thử E2E Tự Động (Automated E2E Test Suite)

Dự án tích hợp bộ kiểm thử hộp đen (black-box) E2E toàn diện được chia làm 4 tầng độc lập:
1. **Tier 1 — Core Features**: Kiểm tra toàn bộ 26 tính năng nghiệp vụ cốt lõi theo `PROJECT.md`.
2. **Tier 2 — Boundary & Validation**: Kiểm tra các giới hạn ký tự, chuỗi Unicode đặc biệt, SQL injection mitigation, và xử lý mã lỗi HTTP 400/404.
3. **Tier 3 — Pairwise Combinations**: Kiểm thử phối hợp các bộ dữ liệu đa chiều (độ dài tin nhắn, trạng thái tác giả, nhiều lượt thích đồng thời).
4. **Tier 4 — Real-world Scenarios**: Mô phỏng kịch bản luồng người dùng thực tế từ tạo bài, tải lại trang, và nhiều lượt thả tim liên tiếp.

### Cách chạy kiểm thử
Khi ứng dụng đang chạy (bằng Docker hoặc cục bộ):
```bash
# Chạy toàn bộ 4 tầng kiểm thử
./tests/e2e/run-e2e-tests.sh

# Hoặc lọc theo tầng kiểm thử cụ thể (ví dụ Tier 1)
./tests/e2e/run-e2e-tests.sh --tier 1

# Bật chế độ chi tiết (Verbose mode)
./tests/e2e/run-e2e-tests.sh --verbose

# Tùy chỉnh URL mục tiêu nếu cổng khác
./tests/e2e/run-e2e-tests.sh --url http://localhost:8080 --frontend http://localhost:3000
```

---

## 6. Cấu Trúc Mã Nguồn (Project Structure)

```
test-deploy/
├── docker-compose.yml           # Cấu hình điều phối cụm Docker containers
├── .gitignore                   # Quy tắc bỏ qua tệp tin mã nguồn & build artifacts
├── README.md                    # Tài liệu hướng dẫn sử dụng sản phẩm
├── sync_all.sh                  # Kịch bản đồng bộ các module vào cấu trúc dự án
├── backend/                     # Mã nguồn máy chủ Spring Boot 3
│   ├── pom.xml                  # Cấu hình Maven, phiên bản Java & thư viện
│   ├── Dockerfile               # Multi-stage Dockerfile cho Backend
│   ├── mvnw / mvnw.cmd          # Maven Wrapper script
│   ├── .mvn/wrapper/            # Cấu hình Maven Wrapper
│   └── src/
│       ├── main/
│       │   ├── java/com/example/confessionwall/
│       │   │   ├── ConfessionWallApplication.java
│       │   │   ├── controller/ConfessionController.java
│       │   │   ├── dto/ConfessionRequest.java, ErrorResponse.java
│       │   │   ├── exception/GlobalExceptionHandler.java, ResourceNotFoundException.java
│       │   │   ├── model/Confession.java
│       │   │   ├── repository/ConfessionRepository.java
│       │   │   └── service/ConfessionService.java, impl/ConfessionServiceImpl.java
│       │   └── resources/application.yml
│       └── test/
│           ├── java/com/example/confessionwall/...
│           └── resources/application-test.yml
├── frontend/                    # Ứng dụng giao diện người dùng React + Vite
│   ├── package.json             # Danh sách gói npm & kịch bản build
│   ├── vite.config.js           # Cấu hình Vite dev server & API proxy
│   ├── tailwind.config.js       # Cấu hình giao diện Tailwind CSS & brand palette
│   ├── postcss.config.js        # Cấu hình PostCSS
│   ├── index.html               # Điểm neo HTML Single Page Application
│   ├── Dockerfile               # Multi-stage Dockerfile cho Frontend (Nginx)
│   ├── nginx.conf               # Cấu hình máy chủ web Nginx & reverse proxy
│   └── src/
│       ├── main.jsx, App.jsx, index.css
│       ├── services/api.js      # Axios client giao tiếp REST API
│       └── components/
│           ├── Header.jsx       # Thanh tiêu đề ứng dụng
│           ├── ConfessionForm.jsx # Biểu mẫu đăng bài với kiểm tra dữ liệu
│           ├── ConfessionCard.jsx # Thẻ hiển thị nội dung & nút thả tim
│           └── ConfessionList.jsx # Lưới danh sách bài viết & skeleton loader
└── tests/
    └── e2e/                     # Bộ kiểm thử E2E tích hợp Tiers 1-4
        ├── run-e2e-tests.sh     # Script khởi chạy kiểm thử
        ├── test-runner.js       # Bộ điều phối thực thi kiểm thử Node.js
        ├── test-utils.js        # Các tiện ích HTTP assertion
        ├── tier1-features.js    # Kiểm thử tính năng cốt lõi
        ├── tier2-boundary.js    # Kiểm thử biên & xác thực dữ liệu
        ├── tier3-pairwise.js    # Kiểm thử tổ hợp cặp
        └── tier4-scenarios.js   # Kiểm thử kịch bản người dùng thực tế
```

---

## 7. English Documentation

### Overview
Confession Wall is a production-ready, fullstack web application featuring an interactive wall where users can anonymously post messages, view recent confessions ordered in reverse chronological order, and react with live heart likes.

### Architecture
- **Frontend Container**: Serves static React SPA assets through Nginx on host port `3000`. Proxies internal `/api/` calls directly to the backend container.
- **Backend Container**: Runs a Spring Boot 3 REST API on port `8080`, performing input validation and database transactions with Spring Data JPA.
- **Postgres Container**: PostgreSQL 15 database on port `5432` with automated health checks (`pg_isready`) and data persistence backed by the named volume `postgres_data`.

### Docker Quickstart
Ensure Docker and Docker Compose are installed, then run:
```bash
docker compose up -d --build
```
Verify container status:
```bash
docker compose ps
```
Access endpoints:
- Frontend Web App: [http://localhost:3000](http://localhost:3000)
- REST API Base: [http://localhost:8080/api/confessions](http://localhost:8080/api/confessions)
- PostgreSQL Database: `localhost:5432`

Shutdown containers:
```bash
docker compose down
# Or wipe database volume:
docker compose down -v
```

### Local Development Setup
1. **PostgreSQL**: Create database `confession_db` owned by user `postgres`.
2. **Backend**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
3. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### API Endpoints
- `GET /api/confessions`: Returns array of confessions sorted newest first.
- `POST /api/confessions`: Creates a new confession. Body `{ "content": "message", "author": "optional name" }`. Validates content non-blank; defaults author to `"Ẩn danh"`. Returns 201 Created or 400 Bad Request.
- `PUT /api/confessions/{id}/like`: Increments like count by 1. Returns 200 OK with updated entity or 404 Not Found if ID does not exist.

### E2E Testing
Execute the complete test suite against a running instance:
```bash
./tests/e2e/run-e2e-tests.sh
```
Options:
- `--tier <1|2|3|4|all>`: Filter by test tier.
- `--filter <regex>`: Filter tests by name or ID.
- `--verbose`: Enable verbose HTTP request/response logging.
# Confession-Wall

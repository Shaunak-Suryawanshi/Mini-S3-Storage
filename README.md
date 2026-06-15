# Enterprise Object Storage Platform (Mini S3)

Mini S3 is a Spring Boot backend project that models the core ideas behind object storage platforms such as AWS S3, MinIO, Google Cloud Storage, and Dropbox.

The current version is a monolithic Java application with JWT security, user-owned buckets, local file storage, sharing links, basic analytics, and a simple Bootstrap frontend.

## Current Scope

This project currently runs with:

- Java 21
- Spring Boot 4
- Spring Security
- JWT authentication
- Spring Data JPA
- Hibernate
- MySQL
- Local filesystem storage
- Bootstrap, HTML, CSS, and JavaScript frontend

Redis, RabbitMQ, MinIO, Docker, Kubernetes, and CI/CD are intentionally deferred for later phases.

## What We Built

### Authentication

- User registration
- User login
- Password hashing with Spring Security
- JWT token generation
- JWT request filtering
- Protected API endpoints

Main files:

- `src/main/java/com/shaunak/mini_s3/controller/AuthController.java`
- `src/main/java/com/shaunak/mini_s3/service/AuthService.java`
- `src/main/java/com/shaunak/mini_s3/security/JwtService.java`
- `src/main/java/com/shaunak/mini_s3/security/JwtAuthenticationFilter.java`
- `src/main/java/com/shaunak/mini_s3/config/SecurityConfig.java`

### User Management

- User entity
- User repository
- Registration DTOs
- User lookup through `CustomUserDetailsService`

Main files:

- `src/main/java/com/shaunak/mini_s3/entity/User.java`
- `src/main/java/com/shaunak/mini_s3/repository/UserRepository.java`
- `src/main/java/com/shaunak/mini_s3/security/CustomUserDetailsService.java`

### Bucket Management

- Create bucket
- List buckets owned by the authenticated user
- View bucket details
- Delete bucket
- Update bucket visibility
- Public and private bucket concept

Main files:

- `src/main/java/com/shaunak/mini_s3/entity/Bucket.java`
- `src/main/java/com/shaunak/mini_s3/controller/BucketController.java`
- `src/main/java/com/shaunak/mini_s3/service/BucketService.java`
- `src/main/java/com/shaunak/mini_s3/repository/BucketRepository.java`

### File Management

- Upload files into a bucket
- Store files locally under the application storage directory
- Store file metadata in MySQL
- List files in a bucket
- Search files by name
- Download files
- Delete files

Main files:

- `src/main/java/com/shaunak/mini_s3/entity/FileMetadata.java`
- `src/main/java/com/shaunak/mini_s3/controller/FileController.java`
- `src/main/java/com/shaunak/mini_s3/service/FileService.java`
- `src/main/java/com/shaunak/mini_s3/repository/FileMetadataRepository.java`
- `src/main/java/com/shaunak/mini_s3/service/storage/StorageService.java`
- `src/main/java/com/shaunak/mini_s3/service/storage/LocalStorageService.java`

### Public Bucket Access

- Public buckets can expose file metadata and downloads without JWT authentication.
- Private buckets remain protected by JWT authentication and ownership checks.

Main file:

- `src/main/java/com/shaunak/mini_s3/controller/PublicBucketController.java`

### Sharing Links

- Generate temporary download links
- Download files using public tokens
- List active links for a file
- Revoke download links

Main files:

- `src/main/java/com/shaunak/mini_s3/entity/DownloadLink.java`
- `src/main/java/com/shaunak/mini_s3/controller/SharingController.java`
- `src/main/java/com/shaunak/mini_s3/service/SharingService.java`
- `src/main/java/com/shaunak/mini_s3/repository/DownloadLinkRepository.java`

### Analytics

- Track authenticated file downloads
- Count file downloads
- Calculate bucket storage usage
- Calculate user storage usage
- List user download activity

Main files:

- `src/main/java/com/shaunak/mini_s3/entity/DownloadEvent.java`
- `src/main/java/com/shaunak/mini_s3/controller/AnalyticsController.java`
- `src/main/java/com/shaunak/mini_s3/service/AnalyticsService.java`
- `src/main/java/com/shaunak/mini_s3/repository/DownloadEventRepository.java`

### Frontend

- Landing page
- Login and registration page
- Dashboard page
- Bucket detail page
- JWT-based API calls from browser JavaScript
- Authenticated file downloads using `fetch` and Blob handling

Main files:

- `src/main/resources/static/index.html`
- `src/main/resources/static/login.html`
- `src/main/resources/static/dashboard.html`
- `src/main/resources/static/bucket.html`
- `src/main/resources/static/js/app.js`

## Architecture

Current architecture is a layered monolith:

```text
Browser / Postman
        |
        v
Controller Layer
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
MySQL Database

File bytes are stored through:

FileController -> FileService -> StorageService -> LocalStorageService -> local disk
```

## Package Structure

```text
com.shaunak.mini_s3
├── config          Spring configuration
├── controller      REST API controllers
├── dto             Request and response objects
├── entity          JPA database entities
├── exception       Custom exceptions and global handler
├── repository      Spring Data JPA repositories
├── security        JWT and authentication logic
├── service         Business logic
└── service/storage Storage abstraction and local storage implementation
```

## API Summary

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### Buckets

- `POST /api/buckets`
- `GET /api/buckets`
- `GET /api/buckets/{bucketId}`
- `PATCH /api/buckets/{bucketId}/visibility`
- `DELETE /api/buckets/{bucketId}`

### Files

- `POST /api/buckets/{bucketId}/files`
- `GET /api/buckets/{bucketId}/files`
- `GET /api/buckets/{bucketId}/files?search={fileName}`
- `GET /api/buckets/{bucketId}/files/{fileId}`
- `GET /api/buckets/{bucketId}/files/{fileId}/download`
- `DELETE /api/buckets/{bucketId}/files/{fileId}`

### Public Files

- `GET /api/public/buckets/{bucketName}/files`
- `GET /api/public/buckets/{bucketName}/files/{fileId}`
- `GET /api/public/buckets/{bucketName}/files/{fileId}/download`

### Sharing

- `POST /api/buckets/{bucketId}/files/{fileId}/links`
- `GET /api/buckets/{bucketId}/files/{fileId}/links`
- `DELETE /api/buckets/{bucketId}/files/{fileId}/links/{linkId}`
- `GET /api/public/downloads/{token}`

### Analytics

- `GET /api/buckets/{bucketId}/files/{fileId}/analytics`
- `GET /api/buckets/{bucketId}/analytics/usage`
- `GET /api/analytics/me/usage`
- `GET /api/analytics/me/downloads`

## Local Setup

### Prerequisites

- Java 21
- MySQL 8
- Maven Wrapper included in the project

### Database

Create a MySQL database:

```sql
CREATE DATABASE mini_s3;
```

Update database credentials in:

```text
src/main/resources/application.properties
```

Configuration is read from environment variables with development defaults:

```properties
DB_URL=jdbc:mysql://localhost:3306/mini_s3
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=replace-with-a-long-random-secret
JWT_EXPIRATION=86400000
```

On PowerShell, set them before running the app:

```powershell
$env:DB_PASSWORD="your_mysql_password"
$env:JWT_SECRET="replace-with-a-long-random-secret"
.\mvnw.cmd spring-boot:run
```

Do not commit real database passwords or production JWT secrets.

### Run Tests

```powershell
.\mvnw.cmd test
```

Expected result:

```text
BUILD SUCCESS
```

### Run Application

```powershell
.\mvnw.cmd spring-boot:run
```

Application starts on:

```text
http://localhost:8081
```

## Storage Behavior

This version uses local filesystem storage instead of MinIO.

Uploaded file bytes are saved under:

```text
storage/
```

File metadata is saved in MySQL through the `FileMetadata` entity.

The `storage/` directory is ignored by Git because uploaded user files should not be committed.

## Security Notes

- JWT is required for protected APIs.
- Public APIs are limited to public bucket downloads and token-based sharing.
- Bucket and file operations are scoped to the authenticated owner.
- The JWT secret in `application.properties` is for development only.
- Production should use environment variables or a secrets manager.

## Deferred Work

The following items are intentionally deferred:

- Redis caching
- RabbitMQ event messaging
- MinIO object storage
- Docker and Kubernetes
- CI/CD pipeline
- Cloud deployment
- Microservices split

## Next Engineering Steps

Recommended next steps:

1. Move database password and JWT secret to environment variables.
2. Add request validation improvements.
3. Add integration tests for auth, buckets, files, and sharing.
4. Add Docker Compose for MySQL and the Spring Boot app.
5. Replace local storage with MinIO when ready for object-storage parity.
6. Add GitHub Actions CI after the repository is pushed.

## Resume Summary

- Designed and developed a Mini S3-style object storage platform using Java 21, Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, MySQL, and local filesystem storage.
- Implemented authenticated bucket and file management APIs covering user registration, login, bucket CRUD, file upload, search, download, delete, public bucket access, and temporary sharing links.
- Built analytics and operational features including download tracking, storage usage reporting, Bootstrap frontend integration, and a roadmap for Docker, CI/CD, and cloud deployment.

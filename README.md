# user-service

A Spring Boot microservice for user authentication and management with JWT token support.

## Features

- ✅ User registration and authentication
- ✅ JWT-based access tokens (1 hour expiration)
- ✅ **Refresh token mechanism** (7 days expiration)
- ✅ Secure password hashing with BCrypt
- ✅ MongoDB integration for user storage
- ✅ Role-based access control (USER, ADMIN, COURIER, RESTAURANT)

## Refresh Token Implementation

This service implements a modern refresh token mechanism that allows users to obtain new access tokens without re-entering credentials. This provides a better user experience while maintaining security.

**Key Benefits:**
- Users don't need to login every hour when access tokens expire
- Secure token rotation prevents token reuse attacks
- Longer session duration (7 days) with automatic token refresh

For detailed information, see [REFRESH_TOKEN_GUIDE.md](REFRESH_TOKEN_GUIDE.md)

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MongoDB running on localhost:27017

### Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/user/login` | POST | Login and get access + refresh tokens |
| `/user/register` | POST | Register a new user |
| `/user/refresh-token` | POST | Get new tokens using refresh token |

## Configuration

Key properties in `application.properties`:
```properties
# JWT Configuration
app.jwt.secret=<base64-encoded-secret>
app.jwt.expiration-ms=3600000          # 1 hour
app.jwt.refresh-expiration-ms=604800000 # 7 days

# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=ftgo
```

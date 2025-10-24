# Refresh Token Implementation Guide

## Overview

This user-service now implements a modern **refresh token mechanism** that allows users to obtain new access tokens without re-entering their credentials every time. This improves user experience while maintaining security.

## How It Works

### Token Types

1. **Access Token**: Short-lived JWT token (1 hour) used to authenticate API requests
2. **Refresh Token**: Longer-lived JWT token (7 days) used to obtain new access tokens

### Authentication Flow

#### 1. Login
```
POST /user/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### 2. Refresh Access Token
When the access token expires (after 1 hour), use the refresh token to get a new one:

```
POST /user/refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Note:** Both tokens are rotated for security. The old refresh token becomes invalid, and a new one is provided.

## Security Features

### 1. Token Rotation
- Each refresh operation generates a **new refresh token**
- The old refresh token is immediately invalidated
- This prevents token reuse attacks

### 2. Database Storage
- Refresh tokens are stored in the MongoDB database
- Tokens are validated against the database on each refresh request
- Ensures tokens can be revoked if needed

### 3. Token Type Validation
- Refresh tokens include a `tokenType: "refresh"` claim
- Access tokens cannot be used as refresh tokens and vice versa

### 4. Expiration Times
- **Access Token**: 1 hour (3600000 ms)
- **Refresh Token**: 7 days (604800000 ms)

### 5. Secure Error Handling
- Generic error messages prevent information disclosure
- Custom `InvalidRefreshTokenException` for token validation failures

## Configuration

Update `application.properties` to customize token expiration:

```properties
# Access token expiration (in milliseconds)
app.jwt.expiration-ms=3600000

# Refresh token expiration (in milliseconds)
app.jwt.refresh-expiration-ms=604800000
```

## Client Implementation Example

### JavaScript/TypeScript Example

```javascript
class AuthService {
  async login(username, password) {
    const response = await fetch('/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    
    const { token, refreshToken } = await response.json();
    
    // Store tokens securely (use httpOnly cookies in production)
    localStorage.setItem('accessToken', token);
    localStorage.setItem('refreshToken', refreshToken);
    
    return { token, refreshToken };
  }
  
  async refreshAccessToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    
    const response = await fetch('/user/refresh-token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    
    if (!response.ok) {
      // Refresh token expired or invalid - redirect to login
      this.logout();
      return null;
    }
    
    const { token, refreshToken: newRefreshToken } = await response.json();
    
    // Update stored tokens
    localStorage.setItem('accessToken', token);
    localStorage.setItem('refreshToken', newRefreshToken);
    
    return token;
  }
  
  async apiCall(url, options = {}) {
    let accessToken = localStorage.getItem('accessToken');
    
    // Add access token to request
    options.headers = {
      ...options.headers,
      'Authorization': `Bearer ${accessToken}`
    };
    
    let response = await fetch(url, options);
    
    // If access token expired, refresh and retry
    if (response.status === 401) {
      accessToken = await this.refreshAccessToken();
      if (accessToken) {
        options.headers.Authorization = `Bearer ${accessToken}`;
        response = await fetch(url, options);
      }
    }
    
    return response;
  }
  
  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    // Redirect to login page
  }
}
```

## Best Practices

### For Production Deployments

1. **Secure Token Storage**
   - Use **httpOnly cookies** for web applications (more secure than localStorage)
   - Use secure storage mechanisms on mobile apps (Keychain/Keystore)

2. **HTTPS Only**
   - Always use HTTPS in production to prevent token interception

3. **Token Revocation**
   - Implement logout functionality that clears the refresh token from the database
   - Consider adding a token blacklist for immediate revocation

4. **Rate Limiting**
   - Implement rate limiting on the refresh-token endpoint
   - Prevent brute force attacks

5. **Monitoring**
   - Log refresh token usage
   - Alert on suspicious patterns (multiple refreshes in short time)

## API Endpoints

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/user/login` | POST | No | Authenticate user and get tokens |
| `/user/register` | POST | No | Register a new user |
| `/user/refresh-token` | POST | No | Exchange refresh token for new tokens |

## Database Schema

### AppUserDocument
```java
{
  "id": "string",
  "username": "string",
  "password": "string (hashed)",
  "email": "string",
  "phoneNumber": "string",
  "roles": ["ROLE_USER", "ROLE_ADMIN", ...],
  "refreshToken": "string (JWT)"
}
```

## Troubleshooting

### Common Issues

**"Invalid or expired refresh token"**
- The refresh token has expired (after 7 days)
- The refresh token was already used (token rotation)
- The refresh token doesn't match the one in the database
- **Solution**: User must login again with credentials

**"Token validation failed"**
- The token signature is invalid
- The token is malformed
- **Solution**: Ensure the token is being sent correctly

## Migration from Old System

If you have existing users:

1. Users will need to **login again** to get refresh tokens
2. Old sessions will continue working until access tokens expire
3. After expiration, users will be prompted to login again

## Future Enhancements

Potential improvements for future versions:

- **Token blacklist**: Redis-based blacklist for instant revocation
- **Device tracking**: Associate refresh tokens with specific devices
- **Multiple sessions**: Allow users to have multiple active refresh tokens
- **Automatic cleanup**: Background job to remove expired refresh tokens from database
- **Remember me**: Extended refresh token expiration for "remember me" feature

package com.ftgo.user.config.security.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "fBsWwVrv0BNVpGIRYAjCeXn8nPCp5nHNfMvQY+PI5H9NE37H6ForsPalQRZi01d7zmfLVxe35UIE1qxwWVZxLw==";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days
    private static final String TEST_USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationInMs", REFRESH_TOKEN_EXPIRATION);
        jwtTokenProvider.init();
    }

    @Test
    void testInit_WithValidSecret_ShouldInitializeSuccessfully() {
        assertDoesNotThrow(() -> jwtTokenProvider.init());
    }

    @Test
    void testInit_WithEmptySecret_ShouldThrowException() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "");
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, provider::init);
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void testInit_WithInvalidBase64Secret_ShouldThrowException() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "not-valid-base64!");
        
        // The Base64 decoder can throw different exceptions, so catch any RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, provider::init);
        assertTrue(exception.getMessage().contains("valid Base64") || exception instanceof io.jsonwebtoken.io.DecodingException);
    }

    @Test
    void testGenerateToken_ShouldReturnValidJwtToken() {
        // Given
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User(TEST_USERNAME, "password", authorities);

        // When
        String token = jwtTokenProvider.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verify token structure
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
    }

    @Test
    void testGenerateToken_ShouldContainCorrectUsername() {
        // Given
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User(TEST_USERNAME, "password", authorities);

        // When
        String token = jwtTokenProvider.generateToken(userDetails);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals(TEST_USERNAME, claims.getSubject());
    }

    @Test
    void testGenerateToken_ShouldContainAuthorities() {
        // Given
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        UserDetails userDetails = new User(TEST_USERNAME, "password", authorities);

        // When
        String token = jwtTokenProvider.generateToken(userDetails);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        @SuppressWarnings("unchecked")
        List<String> authClaims = (List<String>) claims.get("auth");
        assertNotNull(authClaims);
        assertEquals(2, authClaims.size());
        assertTrue(authClaims.contains("ROLE_USER"));
        assertTrue(authClaims.contains("ROLE_ADMIN"));
    }

    @Test
    void testGenerateRefreshToken_ShouldReturnValidToken() {
        // When
        String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        
        // Verify token structure
        String[] parts = refreshToken.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
    }

    @Test
    void testGenerateRefreshToken_ShouldContainTokenTypeClaim() {
        // When
        String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
        
        assertEquals("refresh", claims.get("tokenType", String.class));
        assertEquals(TEST_USERNAME, claims.getSubject());
    }

    @Test
    void testValidateRefreshToken_WithValidToken_ShouldReturnUsername() {
        // Given
        String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        // When
        String username = jwtTokenProvider.validateRefreshToken(refreshToken);

        // Then
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void testValidateRefreshToken_WithAccessToken_ShouldReturnNull() {
        // Given - generate an access token instead of refresh token
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User(TEST_USERNAME, "password", authorities);
        String accessToken = jwtTokenProvider.generateToken(userDetails);

        // When
        String username = jwtTokenProvider.validateRefreshToken(accessToken);

        // Then
        assertNull(username, "Access token should not be valid as refresh token");
    }

    @Test
    void testValidateRefreshToken_WithInvalidToken_ShouldReturnNull() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        String username = jwtTokenProvider.validateRefreshToken(invalidToken);

        // Then
        assertNull(username);
    }

    @Test
    void testValidateRefreshToken_WithMalformedToken_ShouldReturnNull() {
        // Given
        String malformedToken = "not-a-jwt-token";

        // When
        String username = jwtTokenProvider.validateRefreshToken(malformedToken);

        // Then
        assertNull(username);
    }

    @Test
    void testValidateRefreshToken_WithTokenSignedByDifferentKey_ShouldReturnNull() {
        // Given - create a token with a different secret
        String differentSecret = "aGVsbG93b3JsZGhlbGxvd29ybGRoZWxsb3dvcmxkaGVsbG93b3JsZGhlbGxvd29ybGRoZWxsb3dvcmxkaGVsbG93b3JsZGhlbGxvd29ybGQ=";
        SecretKey differentKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(differentSecret));
        
        String tokenWithDifferentKey = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .claim("tokenType", "refresh")
                .signWith(differentKey)
                .compact();

        // When
        String username = jwtTokenProvider.validateRefreshToken(tokenWithDifferentKey);

        // Then
        assertNull(username, "Token signed with different key should be invalid");
    }

    @Test
    void testGenerateRefreshToken_MultipleCallsShouldGenerateDifferentTokens() throws InterruptedException {
        // When
        String token1 = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);
        Thread.sleep(1000); // Ensure different timestamps (1 second)
        String token2 = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        // Then
        assertNotEquals(token1, token2, "Each token should be unique due to different timestamps");
        
        // Verify they decode to different tokens
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        var claims1 = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token1).getBody();
        var claims2 = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token2).getBody();
        assertNotEquals(claims1.getIssuedAt(), claims2.getIssuedAt(), "Tokens should have different issuedAt timestamps");
    }

    @Test
    void testGenerateToken_WithDifferentUsers_ShouldGenerateDifferentTokens() {
        // Given
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails user1 = new User("user1", "password", authorities);
        UserDetails user2 = new User("user2", "password", authorities);

        // When
        String token1 = jwtTokenProvider.generateToken(user1);
        String token2 = jwtTokenProvider.generateToken(user2);

        // Then
        assertNotEquals(token1, token2);
    }
}

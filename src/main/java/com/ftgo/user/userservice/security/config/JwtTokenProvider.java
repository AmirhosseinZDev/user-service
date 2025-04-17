package com.ftgo.user.userservice.security.config;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {


    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;
    private SecretKey key;


    /**
     * Initializes the SecretKey after properties are injected.
     * Validates that the secret key is provided and sufficiently strong.
     */
    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(jwtSecret)) {
            log.error("FATAL: JWT secret key is not configured in properties (app.jwt.secret). Application cannot start securely.");
            throw new IllegalStateException("JWT secret key ('app.jwt.secret') cannot be empty.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            log.error("FATAL: JWT secret key ('app.jwt.secret') is not valid Base64: {}", e.getMessage());
            throw new IllegalStateException("JWT secret key ('app.jwt.secret') must be a valid Base64 encoded string.", e);
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JwtTokenProvider initialized successfully.");
    }

    /**
     * Generates a JWT token for the given UserDetails.
     *
     * @param userDetails The user details object containing username and authorities.
     * @return A JWT token string.
     */
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("auth", authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

}

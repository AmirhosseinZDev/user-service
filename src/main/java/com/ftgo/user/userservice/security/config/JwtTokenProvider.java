package com.ftgo.user.userservice.security.config;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;


@Component
public class JwtTokenProvider {


    // In a production environment, move this secret to a secure external configuration.
    private static final String jwtSecret = "fBsWwVrv0BNVpGIRYAjCeXn8nPCp5nHNfMvQY+PI5H9NE37H6ForsPalQRZi01d7zmfLVxe35UIE1qxwWVZxLw==";
    private static final long jwtExpirationInMs = 3600000; // 1 hour
    private final SecretKey key;


    public JwtTokenProvider() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
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

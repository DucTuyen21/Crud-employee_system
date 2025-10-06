package com.example.managelibary.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.managelibary.model.User;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class JwtUtil {
    private final Algorithm algorithm = Algorithm.HMAC512("your-very-long-secret-key-here-32-or-more-characters");
    public long accessTokenValidity = 3600000; // 1 giờ
    public long refreshTokenValidity = 86400000; // 24 giờ

    public String generateAccessToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withArrayClaim("roles", user.getRoles().toArray(new String[0])) // Lưu dưới dạng mảng
                .withClaim("email", user.getEmail())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidity))
                .sign(algorithm);
    }

    public String generateRefreshToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .sign(algorithm);
    }

    public String getUsernameFromToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            System.err.println("Invalid JWT Token in getUsernameFromToken: " + e.getMessage());
            return null;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("email").asString();
        } catch (JWTVerificationException e) {
            System.err.println("Invalid JWT Token in getEmailFromToken: " + e.getMessage());
            return null;
        }
    }

    public List<String> getRolesFromToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("roles").asList(String.class);
        } catch (JWTVerificationException e) {
            System.err.println("Invalid JWT Token in getRolesFromToken: " + e.getMessage());
            return List.of();
        }
    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            System.err.println("Invalid JWT Token in validateToken: " + e.getMessage());
            return false;
        }
    }
}
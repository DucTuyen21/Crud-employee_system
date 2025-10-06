package com.example.managelibary.controler;

import com.example.managelibary.dto.RegisterRequest;
import com.example.managelibary.jwt.JwtUtil;
import com.example.managelibary.model.User;
import com.example.managelibary.service.UserService;
import com.example.managelibary.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
 @CrossOrigin(value = "*") // Gỡ bỏ hoặc thay bằng cấu hình cụ thể trong SecurityConfig
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public AuthController(JwtUtil jwtUtil, UserService userService, AuditLogService auditLogService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            auditLogService.log("REGISTER", request.getUsername(), "User registered: " + request.getEmail() + " as " + request.getRole());
            return buildResponse(user);
        } catch (RuntimeException e) {
            auditLogService.log("REGISTER_FAILED", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin-count")
    public ResponseEntity<Long> getAdminCount() {
        try {
            long count = userService.countAdminUsers();
            System.out.println("Admin count: " + count); // Debug log
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            System.out.println("Error in admin-count: " + e.getMessage()); // Debug log
            return ResponseEntity.status(500).body(0L);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody User loginRequest) {
        try {
            User user = userService.findByUsername(loginRequest.getUsername());
            if (user == null || !userService.validatePassword(user, loginRequest.getPassword())) {
                auditLogService.log("LOGIN_FAILED", loginRequest.getUsername(), "Invalid credentials");
                return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
            }
            auditLogService.log("LOGIN", user.getUsername(), "User logged in");
            return buildResponse(user);
        } catch (Exception e) {
            auditLogService.log("LOGIN_ERROR", loginRequest.getUsername(), "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String refreshToken) {
        try {
            refreshToken = refreshToken.replace("Bearer ", "");
            if (!jwtUtil.validateToken(refreshToken)) {
                auditLogService.log("TOKEN_REFRESH_FAILED", "Unknown", "Invalid refresh token");
                return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
            }
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            User user = userService.findByUsername(username);
            if (user == null) {
                auditLogService.log("TOKEN_REFRESH_FAILED", "Unknown", "User not found");
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            String newAccessToken = jwtUtil.generateAccessToken(user);
            auditLogService.log("TOKEN_REFRESH", username, "Refreshed access token");
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("accessTokenExpiration", jwtUtil.accessTokenValidity);
            response.put("refreshTokenExpiration", jwtUtil.refreshTokenValidity); // Thêm thông tin
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            auditLogService.log("TOKEN_REFRESH_ERROR", "Unknown", "Internal error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken) {
        try {
            accessToken = accessToken.replace("Bearer ", "");
            if (!jwtUtil.validateToken(accessToken)) {
                auditLogService.log("LOGOUT_FAILED", "Unknown", "Invalid access token");
                return ResponseEntity.status(401).body(Map.of("error", "Invalid access token"));
            }
            String username = jwtUtil.getUsernameFromToken(accessToken);
            auditLogService.log("LOGOUT", username, "User logged out");
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            auditLogService.log("LOGOUT_ERROR", "Unknown", "Internal error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String accessToken) {
        try {
            accessToken = accessToken.replace("Bearer ", "");
            if (!jwtUtil.validateToken(accessToken)) {
                auditLogService.log("PROFILE_VIEW_FAILED", "Unknown", "Invalid access token");
                return ResponseEntity.status(401).body(Map.of("error", "Invalid access token"));
            }
            String username = jwtUtil.getUsernameFromToken(accessToken);
            User user = userService.findByUsername(username);
            if (user == null) {
                auditLogService.log("PROFILE_VIEW_FAILED", "Unknown", "User not found");
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            auditLogService.log("PROFILE_VIEW", username, "Viewed profile");
            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("role", Optional.ofNullable(user.getRoles().iterator().next()).orElse("ROLE_USER")); // Xử lý rỗng
            response.put("enable", user.isEnabled());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            auditLogService.log("PROFILE_VIEW_ERROR", "Unknown", "Internal error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    private ResponseEntity<Map<String, Object>> buildResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("email", user.getEmail());
        response.put("role", Optional.ofNullable(user.getRoles().iterator().next()).orElse("ROLE_USER")); // Xử lý rỗng
        response.put("enable", user.isEnabled());
        response.put("accessTokenExpiration", jwtUtil.accessTokenValidity);
        return ResponseEntity.ok(response);
    }
}
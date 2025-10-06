package com.example.managelibary.service;

import com.example.managelibary.dto.RegisterRequest;
import com.example.managelibary.model.User;
import com.example.managelibary.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Tên đăng nhập hoặc email đã tồn tại");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(new HashSet<>());

        String role = "ROLE_USER";
        if (request.getRole() != null && request.getRole().equals("admin")) {
            if (countAdminUsers() < 2) {
                role = "ROLE_ADMIN";
            } else {
                throw new RuntimeException("Admin limit reached, cannot register as admin");
            }
        }
        user.getRoles().add(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    public long countAdminUsers() {
        return userRepository.countAdminUsers();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
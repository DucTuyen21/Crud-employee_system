package com.example.managelibary.service;

import com.example.managelibary.model.Employee;
import com.example.managelibary.model.User;
import com.example.managelibary.repository.EmployeeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public EmployeeService(EmployeeRepository employeeRepository, UserService userService, AuditLogService auditLogService) {
        this.employeeRepository = employeeRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    public Employee register(Employee employee, Authentication authentication) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        employee.setEnabled(true);
        employee.setRoles(Set.of("ROLE_EMPLOYEE")); // Mặc định
        employee.setFileHash(calculateFileHash(employee.getFilePath()));
        Employee savedEmployee = employeeRepository.save(employee);
        auditLogService.log("CREATE_EMPLOYEE", authentication.getName(), "Registered employee: " + employee.getEmail());
        return savedEmployee;
    }

    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public List<Employee> findAll(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        if (user.getRoles().contains("ROLE_ADMIN")) {
            return employeeRepository.findAll();
        }
        return employeeRepository.findAllByEnabled(true); // Chỉ nhân viên thấy enabled
    }

    public Employee update(Long id, Employee employee, Authentication authentication) {
        Employee existing = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        User user = userService.findByUsername(authentication.getName());
        if (!existing.getEmail().equals(user.getUsername()) && !user.getRoles().contains("ROLE_ADMIN")) {
            throw new RuntimeException("Unauthorized");
        }
        existing.setName(employee.getName());
        existing.setEmail(employee.getEmail());
        existing.setFilePath(employee.getFilePath());
        existing.setFileHash(calculateFileHash(employee.getFilePath()));
        existing.setEnabled(employee.isEnabled());
        Employee updated = employeeRepository.save(existing);
        auditLogService.log("UPDATE_EMPLOYEE", authentication.getName(), "Updated employee: " + id);
        return updated;
    }

    public void delete(Long id, Authentication authentication) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        User user = userService.findByUsername(authentication.getName());
        if (!user.getRoles().contains("ROLE_ADMIN")) {
            throw new RuntimeException("Unauthorized");
        }
        employeeRepository.delete(employee);
        auditLogService.log("DELETE_EMPLOYEE", authentication.getName(), "Deleted employee: " + id);
    }

    private String calculateFileHash(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(Paths.get(filePath)));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating file hash");
        }
    }

    public Employee findById(Long id) {
        return  employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
    }
}
package com.example.managelibary.controler;

import com.example.managelibary.model.Employee;
import com.example.managelibary.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(value = "*") // Tạm thời giữ, sau chuyển sang SecurityConfig
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Employee> create(
            @RequestPart("name") String name,
            @RequestPart("email") String email,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("enabled") String enabled,
            Authentication authentication) {
        boolean isEnabled = Boolean.parseBoolean(enabled);
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setEnabled(isEnabled);

        if (file != null && !file.isEmpty()) {
            try {
                String filePath = saveFile(file);
                employee.setFilePath(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save file: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(employeeService.register(employee, authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Employee> update(
            @PathVariable Long id,
            @RequestPart("name") String name,
            @RequestPart("email") String email,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("enabled") String enabled,
            Authentication authentication) {
        boolean isEnabled = Boolean.parseBoolean(enabled);
        Employee employee = employeeService.findById(id); // Giả sử có phương thức này
        if (employee == null) {
            throw new RuntimeException("Employee not found");
        }

        employee.setName(name);
        employee.setEmail(email);
        employee.setEnabled(isEnabled);

        if (file != null && !file.isEmpty()) {
            try {
                String filePath = saveFile(file);
                employee.setFilePath(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save file: " + e.getMessage());
            }
        } else if (employee.getFilePath() == null) {
            employee.setFilePath(""); // Đặt rỗng nếu không có file và không giữ file cũ
        }

        return ResponseEntity.ok(employeeService.update(id, employee, authentication));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<Employee>> getAll(Authentication authentication) {
        List<Employee> employees = employeeService.findAll(authentication);
        return ResponseEntity.ok(employees); // Đảm bảo trả về đầy đủ field
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Employee> getById(@PathVariable Long id, Authentication authentication) {
        Employee employee = employeeService.findById(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee); // Trả về employee với tất cả field
    }
    @GetMapping("/{email}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Employee> getByEmail(@PathVariable String email, Authentication authentication) {
        Employee employee = employeeService.findByEmail(email);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee); // Trả về employee với tất cả field
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        employeeService.delete(id, authentication);
        return ResponseEntity.ok().build();
    }

    private String saveFile(MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path destination = uploadPath.resolve(fileName).normalize().toAbsolutePath();

        file.transferTo(destination.toFile());
        return "uploads/" + fileName;
    }

    @ExceptionHandler({MissingServletRequestPartException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<String> handleMissingPartException(Exception ex) {
        return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
    }
}
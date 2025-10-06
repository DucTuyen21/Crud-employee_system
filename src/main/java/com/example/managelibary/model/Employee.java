package com.example.managelibary.model;


import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String filePath; // Đường dẫn ảnh nhân viên
    private String fileHash; // SHA-256 của file
    private boolean enabled;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;

    public Employee(Long id, String name, String email, String filePath, String fileHash, boolean enabled, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.filePath = filePath;
        this.fileHash = fileHash;
        this.enabled = enabled;
        this.roles = roles;
    }
    public Employee() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
package com.example.managelibary.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String email;

    public UpdateProfileRequest(String email) {
        this.email = email;
    }

    public UpdateProfileRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

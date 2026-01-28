package com.drugscreen.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String phone;
    private String email;
    private String purpose;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

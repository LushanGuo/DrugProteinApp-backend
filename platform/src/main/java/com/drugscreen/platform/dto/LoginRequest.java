package com.drugscreen.platform.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "手机号或邮箱不能为空")
    private String phoneOrEmail;

    @NotBlank(message = "密码不能为空")
    private String password;
}

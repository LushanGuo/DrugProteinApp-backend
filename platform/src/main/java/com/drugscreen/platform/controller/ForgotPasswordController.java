package com.drugscreen.platform.controller;

import com.drugscreen.platform.dto.ApiResponse;
import com.drugscreen.platform.dto.ResetPasswordRequest;
import com.drugscreen.platform.dto.SendCodeRequest;
import com.drugscreen.platform.dto.VerifyCodeRequest;
import com.drugscreen.platform.service.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * 忘记密码控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/forgot-password")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Void>> sendCode(@Valid @RequestBody SendCodeRequest request, BindingResult bindingResult) {
        // 验证参数
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.error("发送验证码参数验证失败: {}", errorMsg);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMsg));
        }

        try {
            log.info("发送验证码请求: phone={}", request.getPhone());
            forgotPasswordService.sendResetCode(request.getPhone());
            return ResponseEntity.ok(ApiResponse.success("验证码已发送", null));
        } catch (RuntimeException e) {
            log.error("发送验证码失败: phone={}, error={}", request.getPhone(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("发送验证码异常: phone={}", request.getPhone(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("服务器内部错误"));
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Void>> verifyCode(@Valid @RequestBody VerifyCodeRequest request, BindingResult bindingResult) {
        // 验证参数
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.error("验证验证码参数验证失败: {}", errorMsg);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMsg));
        }

        try {
            log.info("验证验证码请求: phone={}", request.getPhone());
            forgotPasswordService.verifyResetCode(request.getPhone(), request.getCode());
            return ResponseEntity.ok(ApiResponse.success("验证成功", null));
        } catch (RuntimeException e) {
            log.error("验证验证码失败: phone={}, error={}", request.getPhone(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("验证验证码异常: phone={}", request.getPhone(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("服务器内部错误"));
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request, BindingResult bindingResult) {
        // 验证参数
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.error("重置密码参数验证失败: {}", errorMsg);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMsg));
        }

        try {
            log.info("重置密码请求: phone={}", request.getPhone());
            forgotPasswordService.resetPassword(
                    request.getPhone(),
                    request.getCode(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
        } catch (RuntimeException e) {
            log.error("重置密码失败: phone={}, error={}", request.getPhone(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("重置密码异常: phone={}", request.getPhone(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("服务器内部错误"));
        }
    }
}

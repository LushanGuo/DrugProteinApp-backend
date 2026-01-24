package com.drugscreen.platform.controller;

import com.drugscreen.platform.dto.ApiResponse;
import com.drugscreen.platform.dto.LoginRequest;
import com.drugscreen.platform.dto.RegisterRequest;
import com.drugscreen.platform.dto.UserResponse;
import com.drugscreen.platform.entity.User;
import com.drugscreen.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        // 验证参数
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.error("注册参数验证失败: {}", errorMsg);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMsg));
        }

        try {
            log.info("用户注册请求: phone={}, email={}", request.getPhone(), request.getEmail());
            
            User user = userService.registerUser(
                    request.getPhone(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getPurpose()
            );

            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setPhone(user.getPhone());
            userResponse.setEmail(user.getEmail());
            userResponse.setPurpose(user.getPurpose());
            userResponse.setIsActive(user.getIsActive());
            userResponse.setCreatedAt(user.getCreatedAt());

            log.info("用户注册成功: userId={}", user.getId());
            return ResponseEntity.ok(ApiResponse.success("注册成功", userResponse));
        } catch (RuntimeException e) {
            log.error("用户注册失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("用户注册异常", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("服务器内部错误"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        // 验证参数
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            log.error("登录参数验证失败: {}", errorMsg);
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMsg));
        }

        try {
            log.info("用户登录请求: phoneOrEmail={}", request.getPhoneOrEmail());
            
            String token = userService.login(request.getPhoneOrEmail(), request.getPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("expiresIn", 86400000);  // 24小时

            log.info("用户登录成功: phoneOrEmail={}", request.getPhoneOrEmail());
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (RuntimeException e) {
            log.error("用户登录失败: phoneOrEmail={}, error={}", request.getPhoneOrEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("用户登录异常: phoneOrEmail={}", request.getPhoneOrEmail(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("服务器内部错误"));
        }
    }

    @GetMapping("/check-phone/{phone}")
    public ResponseEntity<ApiResponse<Boolean>> checkPhone(@PathVariable String phone) {
        try {
            boolean exists = userService.checkPhoneExists(phone);
            return ResponseEntity.ok(ApiResponse.success("查询成功", exists));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败"));
        }
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@PathVariable String email) {
        try {
            boolean exists = userService.checkEmailExists(email);
            return ResponseEntity.ok(ApiResponse.success("查询成功", exists));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询失败"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("认证服务正常运行", "OK"));
    }
}

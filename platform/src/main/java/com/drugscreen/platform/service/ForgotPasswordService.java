package com.drugscreen.platform.service;

import com.drugscreen.platform.entity.User;
import com.drugscreen.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 忘记密码服务
 * 使用内存存储验证码（开发环境）
 * 生产环境建议使用 Redis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 内存存储验证码（开发环境使用）
    // 生产环境应该使用 Redis
    private final Map<String, VerificationCode> codeStore = new ConcurrentHashMap<>();
    private final Map<String, VerificationCode> verifiedCodeStore = new ConcurrentHashMap<>();
    private final Map<String, SendLimit> sendLimitStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> errorCountStore = new ConcurrentHashMap<>();
    
    /**
     * 发送重置密码验证码
     */
    public void sendResetCode(String phone) {
        log.info("开始发送重置密码验证码: phone={}", phone);
        
        // 1. 检查手机号是否注册
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("手机号未注册"));
        
        // 2. 检查发送频率（1分钟内只能发送1次）
        SendLimit limit = sendLimitStore.get(phone);
        if (limit != null && !limit.isExpired()) {
            long remainingSeconds = limit.getRemainingSeconds();
            throw new RuntimeException("请" + remainingSeconds + "秒后再试");
        }
        
        // 3. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 4. 存储验证码（5分钟有效）
        VerificationCode verificationCode = new VerificationCode(code, 5);
        codeStore.put(phone, verificationCode);
        
        // 5. 设置发送频率限制（1分钟）
        sendLimitStore.put(phone, new SendLimit(1));
        
        // 6. 发送短信（开发环境：打印到控制台）
        log.info("=".repeat(60));
        log.info("【验证码】手机号: {}", phone);
        log.info("【验证码】验证码: {}", code);
        log.info("【验证码】有效期: 5分钟");
        log.info("=".repeat(60));
        
        // 生产环境应该调用短信服务
        // smsService.sendResetCode(phone, code);
        
        log.info("验证码发送成功: phone={}", phone);
    }
    
    /**
     * 验证重置密码验证码
     */
    public void verifyResetCode(String phone, String code) {
        log.info("开始验证重置密码验证码: phone={}", phone);
        
        // 1. 获取验证码
        VerificationCode storedCode = codeStore.get(phone);
        
        // 2. 验证码不存在或已过期
        if (storedCode == null || storedCode.isExpired()) {
            log.warn("验证码已过期: phone={}", phone);
            codeStore.remove(phone);
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        
        // 3. 验证码错误
        if (!storedCode.getCode().equals(code)) {
            // 记录错误次数
            int errorCount = errorCountStore.getOrDefault(phone, 0) + 1;
            errorCountStore.put(phone, errorCount);
            
            log.warn("验证码错误: phone={}, errorCount={}", phone, errorCount);
            
            if (errorCount >= 5) {
                // 错误5次，删除验证码
                codeStore.remove(phone);
                errorCountStore.remove(phone);
                throw new RuntimeException("验证码错误次数过多，请重新获取验证码");
            }
            
            throw new RuntimeException("验证码错误，还可以尝试" + (5 - errorCount) + "次");
        }
        
        // 4. 验证成功，标记已验证（延长有效期到10分钟）
        VerificationCode verifiedCode = new VerificationCode(code, 10);
        verifiedCodeStore.put(phone, verifiedCode);
        
        // 清除错误计数
        errorCountStore.remove(phone);
        
        log.info("验证码验证成功: phone={}", phone);
    }
    
    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(String phone, String code, String newPassword) {
        log.info("开始重置密码: phone={}", phone);
        
        // 1. 检查验证码是否已验证
        VerificationCode verifiedCode = verifiedCodeStore.get(phone);
        if (verifiedCode == null || verifiedCode.isExpired()) {
            log.warn("验证码未验证或已过期: phone={}", phone);
            verifiedCodeStore.remove(phone);
            throw new RuntimeException("请先验证验证码");
        }
        
        // 2. 验证码不匹配
        if (!verifiedCode.getCode().equals(code)) {
            log.warn("验证码不匹配: phone={}", phone);
            throw new RuntimeException("验证码不匹配");
        }
        
        // 3. 查找用户
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 4. 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        // 5. 更新密码
        user.setPassword(encodedPassword);
        userRepository.save(user);
        
        // 6. 清除所有相关记录
        codeStore.remove(phone);
        verifiedCodeStore.remove(phone);
        sendLimitStore.remove(phone);
        errorCountStore.remove(phone);
        
        log.info("密码重置成功: phone={}", phone);
    }
    
    /**
     * 验证码数据类
     */
    private static class VerificationCode {
        private final String code;
        private final LocalDateTime expiryTime;
        
        public VerificationCode(String code, int validMinutes) {
            this.code = code;
            this.expiryTime = LocalDateTime.now().plusMinutes(validMinutes);
        }
        
        public String getCode() {
            return code;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
    
    /**
     * 发送限制数据类
     */
    private static class SendLimit {
        private final LocalDateTime expiryTime;
        
        public SendLimit(int validMinutes) {
            this.expiryTime = LocalDateTime.now().plusMinutes(validMinutes);
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
        
        public long getRemainingSeconds() {
            return java.time.Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
        }
    }
}

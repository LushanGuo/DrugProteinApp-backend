package com.drugscreen.platform.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 用于生成 BCrypt 加密的密码
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成常用测试密码
        String[] passwords = {"123456", "password", "test123", "admin123"};
        
        System.out.println("=".repeat(60));
        System.out.println("BCrypt 密码生成器");
        System.out.println("=".repeat(60));
        System.out.println();
        
        for (String password : passwords) {
            String encoded = encoder.encode(password);
            System.out.println("原始密码: " + password);
            System.out.println("加密后:   " + encoded);
            System.out.println();
        }
        
        System.out.println("=".repeat(60));
        System.out.println("SQL 更新语句示例:");
        System.out.println("=".repeat(60));
        System.out.println();
        
        String encoded123456 = encoder.encode("123456");
        System.out.println("-- 重置密码为 123456");
        System.out.println("UPDATE users");
        System.out.println("SET password = '" + encoded123456 + "',");
        System.out.println("    updated_at = CURRENT_TIMESTAMP");
        System.out.println("WHERE phone = 'YOUR_PHONE_NUMBER';");
        System.out.println();
        
        System.out.println("=".repeat(60));
        System.out.println("验证密码:");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // 验证密码
        String testPassword = "123456";
        String testHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean matches = encoder.matches(testPassword, testHash);
        System.out.println("密码: " + testPassword);
        System.out.println("哈希: " + testHash);
        System.out.println("匹配: " + matches);
    }
}

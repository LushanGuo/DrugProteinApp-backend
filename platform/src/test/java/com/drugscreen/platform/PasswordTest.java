package com.drugscreen.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class PasswordTest {

    @Test
    public void testPasswordGeneration() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hash1 = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        System.out.println("=".repeat(60));
        System.out.println("密码测试");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // 测试现有哈希
        System.out.println("测试密码: " + password);
        System.out.println("数据库哈希: " + hash1);
        boolean matches = encoder.matches(password, hash1);
        System.out.println("匹配结果: " + matches);
        System.out.println();
        
        // 生成新哈希
        System.out.println("生成新的哈希值:");
        for (int i = 1; i <= 3; i++) {
            String newHash = encoder.encode(password);
            System.out.println("哈希 " + i + ": " + newHash);
            System.out.println("验证: " + encoder.matches(password, newHash));
            System.out.println();
        }
        
        System.out.println("=".repeat(60));
        System.out.println("SQL 更新语句:");
        System.out.println("=".repeat(60));
        String newHash = encoder.encode(password);
        System.out.println("UPDATE users");
        System.out.println("SET password = '" + newHash + "',");
        System.out.println("    updated_at = CURRENT_TIMESTAMP");
        System.out.println("WHERE phone = '18300899350';");
    }
}

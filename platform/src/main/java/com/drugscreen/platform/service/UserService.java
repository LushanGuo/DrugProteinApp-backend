package com.drugscreen.platform.service;

import com.drugscreen.platform.entity.User;
import com.drugscreen.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public User registerUser(String phone, String email, String password, String purpose) {
        // 检查用户是否已存在
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new RuntimeException("手机号已注册");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("邮箱已注册");
        }

        // 创建新用户
        User user = new User();
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPurpose(purpose);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    public String login(String phoneOrEmail, String password) {
        // 查找用户（支持手机号或邮箱登录）
        Optional<User> user = userRepository.findByPhone(phoneOrEmail);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(phoneOrEmail);
        }

        if (user.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User foundUser = user.get();
        if (!foundUser.getIsActive()) {
            throw new RuntimeException("账户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, foundUser.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        return jwtTokenService.generateToken(foundUser);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean checkPhoneExists(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    public boolean checkEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}

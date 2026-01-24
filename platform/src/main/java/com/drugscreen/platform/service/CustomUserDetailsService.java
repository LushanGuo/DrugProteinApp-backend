package com.drugscreen.platform.service;

import com.drugscreen.platform.entity.User;
import com.drugscreen.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 先尝试用邮箱查找
        Optional<User> user = userRepository.findByEmail(username);

        // 如果找不到，尝试用手机号查找
        if (user.isEmpty()) {
            user = userRepository.findByPhone(username);
        }

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        User foundUser = user.get();

        return org.springframework.security.core.userdetails.User.builder()
                .username(foundUser.getEmail())  // 使用邮箱作为用户名
                .password(foundUser.getPassword())
                .authorities("ROLE_USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!foundUser.getIsActive())
                .build();
    }
}

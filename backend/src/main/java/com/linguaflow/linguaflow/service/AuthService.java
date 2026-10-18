package com.linguaflow.linguaflow.service;

import com.linguaflow.linguaflow.dto.AuthResponseDTO;
import com.linguaflow.linguaflow.dto.LoginRequestDTO;
import com.linguaflow.linguaflow.dto.UserDTO;
import com.linguaflow.linguaflow.entity.User;
import com.linguaflow.linguaflow.repository.UserRepository;
import com.linguaflow.linguaflow.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa!");
        }

        String jwt = tokenProvider.generateToken(user.getUsername());

        UserDTO userDTO = UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();

        return AuthResponseDTO.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(userDTO)
                .build();
    }
}
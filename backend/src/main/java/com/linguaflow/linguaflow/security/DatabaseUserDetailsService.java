package com.linguaflow.linguaflow.security;

import com.linguaflow.linguaflow.entity.User;
import com.linguaflow.linguaflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
                String role = user.getRole().startsWith("ROLE_")
                                ? user.getRole()
                                : "ROLE_" + user.getRole();

                return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                                .password(user.getPasswordHash())
                                .authorities(role)
                                .disabled(!Boolean.TRUE.equals(user.getActive()))
                                .build();
        }
}
package com.shaunak.mini_s3.service;


import com.shaunak.mini_s3.dto.RegisterRequest;
import com.shaunak.mini_s3.entity.User;
import com.shaunak.mini_s3.exception.DuplicateResourceException;
import com.shaunak.mini_s3.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Email already in use");
        }

        if(userRepository.existsByUsername(request.getUsername())){
            throw new DuplicateResourceException("Username already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }
}

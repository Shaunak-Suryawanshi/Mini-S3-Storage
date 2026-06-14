package com.shaunak.mini_s3.controller;


import com.shaunak.mini_s3.dto.RegisterRequest;
import com.shaunak.mini_s3.dto.RegisterResponse;
import com.shaunak.mini_s3.entity.User;
import com.shaunak.mini_s3.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest request){
        User savedUser = userService.registerUser(request);
        RegisterResponse response = new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                "User registered successfully"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

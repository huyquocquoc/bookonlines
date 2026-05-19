package com.bookstore.auth.service;

import com.bookstore.auth.entity.User;
import com.bookstore.auth.entity.UserRole;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.auth.repository.UserRoleRepository;
import com.bookstore.auth.security.JwtUtil;
import com.bookstore.common.dto.AuthResponse;
import com.bookstore.common.dto.LoginRequest;
import com.bookstore.common.dto.SignupRequest;
import com.bookstore.common.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for authentication operations.
 */
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private static final String DEFAULT_ROLE_NAME = "USER_ROLE";

    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        UserRole userRole = getOrCreateDefaultUserRole();

        Set<UserRole> roles = new HashSet<>();
        roles.add(userRole);

        User savedUser = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(roles)
                .active(true)
                .build());

        log.info("User registered successfully: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(savedUser))
                .tokenType("Bearer")
                .user(convertToDTO(savedUser))
                .build();
    }

    public AuthResponse signin(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.info("User logged in successfully: {}", authentication.getName());

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user))
                .tokenType("Bearer")
                .user(convertToDTO(user))
                .build();
    }

    private UserDTO convertToDTO(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roleNames)
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserRole getOrCreateDefaultUserRole() {
        return userRoleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseGet(() -> userRoleRepository.save(UserRole.builder()
                        .name(DEFAULT_ROLE_NAME)
                        .description("Default user role")
                        .build()));
    }
}

// Made with Bob

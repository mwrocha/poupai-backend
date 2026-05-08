package io.poupai.backend.auth.service;

import io.poupai.backend.auth.dto.AuthDtos;
import io.poupai.backend.shared.security.JwtService;
import io.poupai.backend.user.model.User;
import io.poupai.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()
                && userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username já está em uso");
        }

        User.UserBuilder builder = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()));

        if (request.getUsername() != null && !request.getUsername().isBlank())
            builder.username(request.getUsername());
        if (request.getFirstName() != null && !request.getFirstName().isBlank())
            builder.firstName(request.getFirstName());
        if (request.getLastName() != null && !request.getLastName().isBlank())
            builder.lastName(request.getLastName());
        if (request.getBirthDate() != null && !request.getBirthDate().isBlank())
            builder.birthDate(LocalDate.parse(request.getBirthDate()));
        if (request.getProfileImageUrl() != null)
            builder.profileImageUrl(request.getProfileImageUrl());
        if (request.getCpf() != null && !request.getCpf().isBlank())
            builder.cpf(request.getCpf());
        if (request.getPhone() != null && !request.getPhone().isBlank())
            builder.phone(request.getPhone());

        User saved = userRepository.save(builder.build());
        String token = jwtService.generateToken(buildUserDetails(saved));
        return toAuthResponse(saved, token);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        String token = jwtService.generateToken(buildUserDetails(user));
        return toAuthResponse(user, token);
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();
    }

    private AuthDtos.AuthResponse toAuthResponse(User user, String token) {
        return AuthDtos.AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .birthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : null)
                .profileImageUrl(user.getProfileImageUrl())
                .cpf(user.getCpf())
                .phone(user.getPhone())
                .token(token)
                .build();
    }
}
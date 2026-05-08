package io.poupai.backend.user.service;

import io.poupai.backend.user.dto.UserDtos;
import io.poupai.backend.user.model.User;
import io.poupai.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDtos.UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toResponse(user);
    }

    public UserDtos.UserResponse updateProfile(String email, UserDtos.UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(user.getId())) {
                            throw new RuntimeException("Username já está em uso");
                        }
                    });
            user.setUsername(request.getUsername());
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getCpf() != null) user.setCpf(request.getCpf());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        if (request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            user.setBirthDate(LocalDate.parse(request.getBirthDate()));
        }

        return toResponse(userRepository.save(user));
    }

    // Atualizar email — invalida sessão atual (o token continua válido até expirar,
    // mas o subject do JWT não vai mais bater com o email novo, forçando novo login)
    public UserDtos.UserResponse updateEmail(String currentEmail, UserDtos.UpdateEmailRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verifica senha atual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Senha incorreta");
        }

        // Verifica se novo email já está em uso
        userRepository.findByEmail(request.getNewEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new RuntimeException("E-mail já está em uso");
            }
        });

        user.setEmail(request.getNewEmail());
        return toResponse(userRepository.save(user));
    }

    private UserDtos.UserResponse toResponse(User user) {
        return UserDtos.UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .birthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : null)
                .profileImageUrl(user.getProfileImageUrl())
                .cpf(user.getCpf())
                .phone(user.getPhone())
                .build();
    }
}
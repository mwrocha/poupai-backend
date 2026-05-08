package io.poupai.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDtos {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        private String password;

        private String username;
        private String firstName;
        private String lastName;
        private String birthDate;
        private String profileImageUrl;
        private String cpf;
        private String phone;
    }

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthResponse {
        private String id;
        private String email;
        private String username;
        private String firstName;
        private String lastName;
        private String birthDate;
        private String profileImageUrl;
        private String cpf;
        private String phone;
        private String token;
    }
}
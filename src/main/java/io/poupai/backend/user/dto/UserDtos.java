package io.poupai.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private String id;
        private String email;
        private String username;
        private String firstName;
        private String lastName;
        private String birthDate;
        private String profileImageUrl;
        private String cpf;
        private String phone;
    }

    @Data
    public static class UpdateProfileRequest {
        private String username;
        private String firstName;
        private String lastName;
        private String birthDate;
        private String profileImageUrl;
        private String cpf;
        private String phone;
    }

    @Data
    public static class UpdateEmailRequest {
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String newEmail;

        @NotBlank(message = "Senha atual é obrigatória para alterar o e-mail")
        private String currentPassword;
    }
}
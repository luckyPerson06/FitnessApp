package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на вход в систему")
public class LoginRequest {

    @Schema(description = "Email пользователя", example = "ivan@mail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Неверный формат email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @Schema(description = "Пароль", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Пароль обязателен")
    private String password;
}

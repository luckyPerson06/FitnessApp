package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на регистрацию нового клиента")
public class RegisterRequest {

    @Schema(description = "Имя клиента", example = "Иван", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @Schema(description = "Отчество клиента", example = "Иванович")
    private String middleName;

    @Schema(description = "Фамилия клиента", example = "Иванов", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @Schema(description = "Номер телефона", example = "+79991234567", pattern = "^\\+?\\d{10,15}$")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Неверный формат телефона")
    private String phoneNumber;

    @Schema(description = "Email клиента", example = "ivan@mail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Неверный формат email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @Schema(description = "Пароль", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Пароль обязателен")
    private String password;
}

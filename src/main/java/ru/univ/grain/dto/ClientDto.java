package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на создание клиента")
public class ClientDto {

    @Schema(description = "Имя клиента", example = "Иван", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @Schema(description = "Отчество клиента", example = "Иванович")
    private String middleName;

    @Schema(description = "Фамилия клиента", example = "Иванов", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @Schema(description = "Номер телефона", example = "+375291234567", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Неверный формат телефона")
    private String phoneNumber;

    @Schema(description = "Email клиента", example = "ivan@mail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Неверный формат email")
    @NotBlank(message = "Email обязателен")
    private String email;
}

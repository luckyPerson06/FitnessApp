package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import ru.univ.grain.entities.ClientStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ с данными клиента")
public class ClientResponseDto {

    @Schema(description = "ID клиента", example = "1")
    @NotNull(message = "ID клиента обязателен")
    private long id;

    @Schema(description = "Полное имя клиента", example = "Иванов Иван Иванович")
    @NotBlank(message = "ФИО клиента обязательно")
    private String fullName;

    @Schema(description = "Номер телефона", example = "+79991234567")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Неверный формат телефона")
    private String phoneNumber;

    @Schema(description = "Email клиента", example = "ivan@mail.com")
    @Email(message = "Неверный формат email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @Schema(description = "Статус клиента", example = "ACTIVE")
    @NotNull(message = "Статус клиента обязателен")
    private ClientStatus status;
}

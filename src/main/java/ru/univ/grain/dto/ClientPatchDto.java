package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import ru.univ.grain.entities.ClientStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на частичное обновление клиента")
public class ClientPatchDto {

    @Schema(description = "Имя клиента", example = "Иван")
    private String firstName;

    @Schema(description = "Отчество клиента", example = "Иванович")
    private String middleName;

    @Schema(description = "Фамилия клиента", example = "Иванов")
    private String lastName;

    @Schema(description = "Номер телефона", example = "+375291234567", pattern = "^\\+?\\d{10,15}$")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Неверный формат телефона")
    private String phoneNumber;

    @Schema(description = "Email клиента", example = "ivan@mail.com")
    @Email(message = "Неверный формат email")
    private String email;

    @Schema(description = "Статус клиента", example = "ACTIVE")
    private ClientStatus status;
}

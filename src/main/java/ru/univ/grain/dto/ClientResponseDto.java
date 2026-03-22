package ru.univ.grain.dto;

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
public class ClientResponseDto {

    @NotNull(message = "ID клиента обязателен")
    private long id;

    @NotBlank(message = "ФИО клиента обязательно")
    private String fullName;

    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Неверный формат телефона")
    private String phoneNumber;

    @Email(message = "Неверный формат email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @NotNull(message = "Статус клиента обязателен")
    private ClientStatus status;
}

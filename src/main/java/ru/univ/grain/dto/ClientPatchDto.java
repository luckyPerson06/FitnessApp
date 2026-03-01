package ru.univ.grain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import ru.univ.grain.entities.ClientStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPatchDto {
    private String firstName;
    private String middleName;
    private String lastName;

    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Неверный формат телефона")
    private String phoneNumber;

    @Email(message = "Неверный формат email")
    private String email;
    private String password;

    private ClientStatus status;
}

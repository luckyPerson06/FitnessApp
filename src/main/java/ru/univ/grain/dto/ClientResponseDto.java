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
    @NotNull
    private long id;

    @NotBlank
    private String fullName;

    @Pattern(regexp = "^\\+?\\d{10,15}$")
    private String phoneNumber;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private ClientStatus status;
}

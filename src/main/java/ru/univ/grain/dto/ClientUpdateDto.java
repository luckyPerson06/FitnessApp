package ru.univ.grain.dto;

import lombok.*;
import ru.univ.grain.domain.ClientStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientUpdateDto {
    private String firstName;
    private String middleName;
    private String lastName;

    private String phoneNumber;
    private String email;

    private ClientStatus status;
}

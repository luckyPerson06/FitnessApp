package ru.univ.grain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCreateDto {
    private String firstName;

    private String middleName;

    private String lastName;

    private String phoneNumber;

    private String email;
}

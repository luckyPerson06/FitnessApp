package ru.univ.grain.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    private long id;

    private String firstName;
    private String middleName;
    private String lastName;

    private String phoneNumber;
    private String email;
    private String password;

    private ClientStatus status;
}

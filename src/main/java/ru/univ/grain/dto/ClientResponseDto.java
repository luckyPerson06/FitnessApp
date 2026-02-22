package ru.univ.grain.dto;

import lombok.*;
import ru.univ.grain.domain.ClientStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDto {
    private long id;

    private String fullName;
    private String phoneNumber;

    private ClientStatus status;
}

package ru.univ.grain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDto {
    private long id;
    private String fullName;
    private String phoneNumber;
}

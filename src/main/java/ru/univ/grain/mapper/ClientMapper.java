package ru.univ.grain.mapper;

import ru.univ.grain.domain.Client;
import ru.univ.grain.dto.ClientCreateDto;
import ru.univ.grain.dto.ClientResponseDto;

import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    public ClientResponseDto toDto(Client client) {
        return ClientResponseDto.builder()
                .id(client.getId())
                .fullName(client.getLastName() + " " + client.getFirstName())
                .phoneNumber(client.getPhoneNumber())
                .build();
    }

    public Client toEntity(ClientCreateDto dto, long id) {
        return Client.builder()
                .id(id)
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .build();
    }
}

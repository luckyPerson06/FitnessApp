package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.ClientDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.entities.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "fullName", expression = "java(client.getLastName()"
            + " + \" \" + client.getFirstName() + (client.getMiddleName()"
            + " != null ? \" \" + client.getMiddleName() : \"\"))")
    ClientResponseDto toResponseDto(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "visits", ignore = true)
    Client toEntity(ClientDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    void updateEntity(ClientPatchDto dto, @MappingTarget Client client);

}

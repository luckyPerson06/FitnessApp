package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.entities.Visit;

@Mapper(componentModel = "spring")
public interface VisitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "workoutSession", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", constant = "BOOKED")
    Visit toEntity(VisitDto dto);

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "workoutSessionId", source = "workoutSession.id")
    @Mapping(target = "subscriptionId", source = "subscription.id")
    @Mapping(target = "status", source = "status")
    VisitDto toDto(Visit visit);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "workoutSession", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(VisitDto dto, @MappingTarget Visit visit);
}

package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.Subscription;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "allowedWorkoutTypes", ignore = true)
    @Mapping(target = "clients", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Subscription toEntity(SubscriptionDto dto);

    SubscriptionDto toDto(Subscription subscription);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "allowedWorkoutTypes", ignore = true)
    @Mapping(target = "clients", ignore = true)
    void updateEntity(SubscriptionDto dto, @MappingTarget Subscription subscription);
}

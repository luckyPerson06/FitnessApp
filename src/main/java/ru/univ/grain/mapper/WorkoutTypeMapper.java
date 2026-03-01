package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.entities.WorkoutType;

@Mapper(componentModel = "spring")
public interface WorkoutTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "workoutSessions", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    WorkoutType toEntity(WorkoutTypeDto dto);

    WorkoutTypeDto toDto(WorkoutType workoutType);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "workoutSessions", ignore = true)
    void updateEntity(WorkoutTypeDto dto, @MappingTarget WorkoutType workoutType);
}

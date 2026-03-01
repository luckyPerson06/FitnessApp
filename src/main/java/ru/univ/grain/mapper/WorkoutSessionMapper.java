package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.entities.WorkoutSession;

@Mapper(componentModel = "spring")
public interface WorkoutSessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "workoutType", ignore = true)
    @Mapping(target = "visits", ignore = true)
    @Mapping(target = "status", constant = "SCHEDULED")
    WorkoutSession toEntity(WorkoutSessionDto dto);

    @Mapping(target = "trainerId", source = "trainer.id")
    @Mapping(target = "workoutTypeId", source = "workoutType.id")
    WorkoutSessionDto toDto(WorkoutSession session);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "workoutType", ignore = true)
    @Mapping(target = "visits", ignore = true)
    void updateEntity(WorkoutSessionDto dto, @MappingTarget WorkoutSession session);
}

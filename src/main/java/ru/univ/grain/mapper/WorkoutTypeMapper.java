package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.entities.Trainer;
import ru.univ.grain.entities.WorkoutType;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkoutTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "workoutSessions", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    WorkoutType toEntity(WorkoutTypeDto dto);

    @Mapping(target = "trainerIds", source = "trainers", qualifiedByName = "trainersToIds")
    WorkoutTypeDto toDto(WorkoutType workoutType);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "workoutSessions", ignore = true)
    void updateEntity(WorkoutTypeDto dto, @MappingTarget WorkoutType workoutType);

    @Named("trainersToIds")
    default List<Long> trainersToIds(List<Trainer> trainers) {
        if (trainers == null) {
            return List.of();
        }
        return trainers.stream().map(Trainer::getId).toList();
    }
}

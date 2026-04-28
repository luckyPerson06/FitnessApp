package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.Subscription;
import ru.univ.grain.entities.WorkoutType;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "allowedWorkoutTypes", ignore = true)
    @Mapping(target = "clients", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Subscription toEntity(SubscriptionDto dto);

    @Mapping(target = "workoutTypeIds", source = "allowedWorkoutTypes", qualifiedByName = "workoutTypesToIds")
    SubscriptionDto toDto(Subscription subscription);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "allowedWorkoutTypes", ignore = true)
    @Mapping(target = "clients", ignore = true)
    void updateEntity(SubscriptionDto dto, @MappingTarget Subscription subscription);

    @Named("workoutTypesToIds")
    default List<Long> workoutTypesToIds(List<WorkoutType> types) {
        if (types == null) {
            return List.of();
        }
        return types.stream().map(WorkoutType::getId).toList();
    }
}

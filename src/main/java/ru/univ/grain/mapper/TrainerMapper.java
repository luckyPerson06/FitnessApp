package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.entities.Trainer;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specializations", ignore = true)
    @Mapping(target = "workoutSessions", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Trainer toEntity(TrainerDto dto);

    TrainerDto toDto(Trainer trainer);

    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specializations", ignore = true)
    @Mapping(target = "workoutSessions", ignore = true)
    void updateEntity(TrainerDto dto, @MappingTarget Trainer trainer);
}

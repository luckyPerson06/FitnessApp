package ru.univ.grain.mapper;

import org.mapstruct.*;
import ru.univ.grain.dto.ClubInfoDto;
import ru.univ.grain.entities.ClubInfo;

@Mapper(componentModel = "spring")
public interface ClubInfoMapper {

    ClubInfoDto toDto(ClubInfo clubInfo);

    @Mapping(target = "id", ignore = true)
    ClubInfo toEntity(ClubInfoDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(ClubInfoDto dto, @MappingTarget ClubInfo clubInfo);
}

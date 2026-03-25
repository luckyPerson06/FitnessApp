package ru.univ.grain.mapper;

import org.junit.jupiter.api.Test;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.entities.WorkoutCategory;
import ru.univ.grain.entities.WorkoutType;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutTypeMapperTest {

    private final WorkoutTypeMapper workoutTypeMapper = new WorkoutTypeMapperImpl();

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name("Йога")
                .description("Хатха-йога для начинающих")
                .iconPath("/icons/yoga.png")
                .category(WorkoutCategory.GROUP)
                .build();

        WorkoutType result = workoutTypeMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Йога");
        assertThat(result.getDescription()).isEqualTo("Хатха-йога для начинающих");
        assertThat(result.getIconPath()).isEqualTo("/icons/yoga.png");
        assertThat(result.getCategory()).isEqualTo(WorkoutCategory.GROUP);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getTrainers()).isNull();
        assertThat(result.getSubscriptions()).isNull();
        assertThat(result.getWorkoutSessions()).isNull();
    }

    @Test
    void toDto_ShouldMapEntityToDto() {
        WorkoutType workoutType = WorkoutType.builder()
                .id(1L)
                .name("Йога")
                .description("Хатха-йога для начинающих")
                .iconPath("/icons/yoga.png")
                .category(WorkoutCategory.GROUP)
                .isActive(true)
                .trainers(new ArrayList<>())
                .subscriptions(new ArrayList<>())
                .workoutSessions(new ArrayList<>())
                .build();

        WorkoutTypeDto result = workoutTypeMapper.toDto(workoutType);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Йога");
        assertThat(result.getDescription()).isEqualTo("Хатха-йога для начинающих");
        assertThat(result.getIconPath()).isEqualTo("/icons/yoga.png");
        assertThat(result.getCategory()).isEqualTo(WorkoutCategory.GROUP);
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void updateEntity_ShouldUpdateOnlyNonNullFields() {
        WorkoutType workoutType = WorkoutType.builder()
                .id(1L)
                .name("Йога")
                .description("Старое описание")
                .isActive(true)
                .build();

        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .description("Новое описание")
                .iconPath("/new/path.png")
                .build();

        workoutTypeMapper.updateEntity(dto, workoutType);

        assertThat(workoutType.getName()).isEqualTo("Йога");
        assertThat(workoutType.getDescription()).isEqualTo("Новое описание");
        assertThat(workoutType.getIconPath()).isEqualTo("/new/path.png");
        assertThat(workoutType.getIsActive()).isTrue();
    }

    @Test
    void updateEntity_ShouldIgnoreNullFields() {
        WorkoutType workoutType = WorkoutType.builder()
                .id(1L)
                .name("Йога")
                .description("Описание")
                .isActive(true)
                .build();

        WorkoutTypeDto dto = WorkoutTypeDto.builder()
                .name(null)
                .description(null)
                .build();

        workoutTypeMapper.updateEntity(dto, workoutType);

        assertThat(workoutType.getName()).isEqualTo("Йога");
        assertThat(workoutType.getDescription()).isEqualTo("Описание");
    }
}
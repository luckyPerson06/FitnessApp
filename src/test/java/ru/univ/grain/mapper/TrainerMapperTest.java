package ru.univ.grain.mapper;

import org.junit.jupiter.api.Test;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.entities.Trainer;
import ru.univ.grain.entities.TrainerStatus;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerMapperTest {

    private final TrainerMapper trainerMapper = new TrainerMapperImpl();

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        TrainerDto dto = TrainerDto.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .photoPath("/images/trainer.jpg")
                .description("Опытный тренер")
                .build();

        Trainer result = trainerMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getFirstName()).isEqualTo("Анна");
        assertThat(result.getLastName()).isEqualTo("Смирнова");
        assertThat(result.getPhotoPath()).isEqualTo("/images/trainer.jpg");
        assertThat(result.getDescription()).isEqualTo("Опытный тренер");
        assertThat(result.getStatus()).isEqualTo(TrainerStatus.ACTIVE);
        assertThat(result.getSpecializations()).isNull();
        assertThat(result.getWorkoutSessions()).isNull();
    }

    @Test
    void toDto_ShouldMapEntityToDto() {
        Trainer trainer = Trainer.builder()
                .id(1L)
                .firstName("Анна")
                .lastName("Смирнова")
                .photoPath("/images/trainer.jpg")
                .description("Опытный тренер")
                .status(TrainerStatus.ACTIVE)
                .build();

        TrainerDto result = trainerMapper.toDto(trainer);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Анна");
        assertThat(result.getLastName()).isEqualTo("Смирнова");
        assertThat(result.getPhotoPath()).isEqualTo("/images/trainer.jpg");
        assertThat(result.getDescription()).isEqualTo("Опытный тренер");
        assertThat(result.getStatus()).isEqualTo(TrainerStatus.ACTIVE);
    }

    @Test
    void updateEntity_ShouldUpdateOnlyNonNullFields() {
        Trainer trainer = Trainer.builder()
                .firstName("Анна")
                .lastName("Смирнова")
                .description("Старое описание")
                .build();

        TrainerDto dto = TrainerDto.builder()
                .description("Новое описание")
                .photoPath("/new/path.jpg")
                .build();

        trainerMapper.updateEntity(dto, trainer);

        assertThat(trainer.getFirstName()).isEqualTo("Анна");
        assertThat(trainer.getLastName()).isEqualTo("Смирнова");
        assertThat(trainer.getDescription()).isEqualTo("Новое описание");
        assertThat(trainer.getPhotoPath()).isEqualTo("/new/path.jpg");
    }
}
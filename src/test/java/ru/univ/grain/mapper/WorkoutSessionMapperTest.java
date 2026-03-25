package ru.univ.grain.mapper;

import org.junit.jupiter.api.Test;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.entities.*;
import ru.univ.grain.entities.WorkoutSession;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutSessionMapperTest {

    private final WorkoutSessionMapper workoutSessionMapper = new WorkoutSessionMapperImpl();

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .trainerId(1L)
                .workoutTypeId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .colorCode("#FF5733")
                .build();

        WorkoutSession result = workoutSessionMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(11, 30));
        assertThat(result.getMaxParticipants()).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo(WorkoutSessionStatus.SCHEDULED);
        assertThat(result.getColorCode()).isEqualTo("#FF5733");
        assertThat(result.getVisits()).isNull();
    }

    @Test
    void toDto_ShouldMapEntityToDto() {
        Trainer trainer = Trainer.builder().id(1L).build();
        WorkoutType workoutType = WorkoutType.builder().id(1L).build();

        WorkoutSession session = WorkoutSession.builder()
                .id(1L)
                .trainer(trainer)
                .workoutType(workoutType)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .maxParticipants(10)
                .status(WorkoutSessionStatus.CONFIRMED)
                .colorCode("#FF5733")
                .visits(new ArrayList<>())
                .build();

        WorkoutSessionDto result = workoutSessionMapper.toDto(session);

        assertThat(result).isNotNull();
        assertThat(result.getTrainerId()).isEqualTo(1L);
        assertThat(result.getWorkoutTypeId()).isEqualTo(1L);
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(11, 30));
        assertThat(result.getMaxParticipants()).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo(WorkoutSessionStatus.CONFIRMED);
        assertThat(result.getColorCode()).isEqualTo("#FF5733");
    }

    @Test
    void updateEntity_ShouldUpdateOnlyNonNullFields() {
        WorkoutSession session = WorkoutSession.builder()
                .id(1L)
                .maxParticipants(10)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .build();

        WorkoutSessionDto dto = WorkoutSessionDto.builder()
                .maxParticipants(15)
                .colorCode("#00FF00")
                .build();

        workoutSessionMapper.updateEntity(dto, session);

        assertThat(session.getMaxParticipants()).isEqualTo(15);
        assertThat(session.getColorCode()).isEqualTo("#00FF00");
        assertThat(session.getStartTime()).isEqualTo(LocalTime.of(10, 0));
    }
}
package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.univ.grain.entities.WorkoutSessionStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Тренировка")
public class WorkoutSessionDto {

    @Schema(description = "ID тренировки", example = "1")
    private Long id;

    @Schema(description = "ID тренера", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long trainerId;

    @Schema(description = "ID типа тренировки", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long workoutTypeId;

    @Schema(description = "День недели", example = "MONDAY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private DayOfWeek dayOfWeek;

    @Schema(description = "Конкретная дата (для разовых тренировок)", example = "2026-04-14")
    private LocalDate sessionDate;

    @Schema(description = "Время начала", example = "09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Время начала обязательно")
    private LocalTime startTime;

    @Schema(description = "Время окончания", example = "10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Время окончания обязательно")
    private LocalTime endTime;

    @Schema(description = "Максимальное количество участников", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Максимальное количество участников обязательно")
    @Min(value = 1, message = "Максимальное количество участников должно быть не менее 1")
    private Integer maxParticipants;

    @Schema(description = "Статус тренировки", example = "SCHEDULED")
    private WorkoutSessionStatus status;

    @Schema(description = "Цветовой код для календаря", example = "#FF5733")
    private String colorCode;

    @Schema(description = "Повторяющаяся тренировка", example = "true")
    private Boolean isRecurring = true;

    @Schema(description = "Повторять до даты", example = "2026-06-30")
    private LocalDate recurringUntil;

    @Schema(description = "Название зала", example = "ЗЕМЛЯ")
    private String room;
}

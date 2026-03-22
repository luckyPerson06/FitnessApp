package ru.univ.grain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.univ.grain.entities.*;
import java.time.DayOfWeek;
import java.time.LocalTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSessionDto {
    @NotNull
    private Long trainerId;

    @NotNull
    private Long workoutTypeId;

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Время начала обязательно")
    private LocalTime startTime;

    @NotNull(message = "Время окончания обязательно")
    private LocalTime endTime;

    @NotNull(message = "Максимальное количество участников обязательно")
    @Min(value = 1, message = "Максимальное количество участников должно быть не менее 1")
    private Integer maxParticipants;

    private WorkoutSessionStatus status;
    private String colorCode;
}

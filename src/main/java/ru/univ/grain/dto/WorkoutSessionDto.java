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

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @Min(1)
    private Integer maxParticipants;

    private WorkoutSessionStatus status;
    private String colorCode;
}

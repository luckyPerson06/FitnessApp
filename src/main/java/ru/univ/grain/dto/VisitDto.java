package ru.univ.grain.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.univ.grain.entities.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitDto {
    @NotNull
    private Long clientId;

    @NotNull
    private Long workoutSessionId;

    private Long subscriptionId;

    @FutureOrPresent
    private LocalDateTime visitTime;

    private VisitStatus status = VisitStatus.BOOKED;
}

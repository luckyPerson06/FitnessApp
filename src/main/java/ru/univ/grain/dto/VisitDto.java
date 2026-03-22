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

    @NotNull(message = "ID клиента обязателен")
    private Long clientId;

    @NotNull(message = "ID тренировки обязателен")
    private Long workoutSessionId;

    private Long subscriptionId;

    @FutureOrPresent(message = "Время визита не может быть в прошлом")
    private LocalDateTime visitTime;

    private VisitStatus status = VisitStatus.BOOKED;
}

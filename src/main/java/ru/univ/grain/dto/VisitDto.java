package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запрос на создание/обновление визита")
public class VisitDto {

    @Schema(description = "ID клиента", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID клиента обязателен")
    private Long clientId;

    @Schema(description = "ID тренировки", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID тренировки обязателен")
    private Long workoutSessionId;

    @Schema(description = "ID использованного абонемента (опционально)", example = "2")
    private Long subscriptionId;

    @Schema(description = "Дата и время визита", example = "2026-03-25T10:00:00")
    @FutureOrPresent(message = "Время визита не может быть в прошлом")
    private LocalDateTime visitTime;

    @Schema(description = "Статус визита", example = "BOOKED")
    private VisitStatus status = VisitStatus.BOOKED;
}

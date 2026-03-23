package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на создание/обновление абонемента")
public class SubscriptionDto {

    @Schema(description = "Название абонемента", example = "Базовый", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Название абонемента обязательно")
    private String name;

    @Schema(description = "Описание абонемента", example = "8 посещений в месяц")
    private String description;

    @Schema(description = "Цена абонемента", example = "3000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Цена абонемента обязательна")
    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;

    @Schema(description = "Тип абонемента", example = "LIMITED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Тип абонемента обязателен")
    private SubscriptionType subscriptionType;

    @Schema(description = "Максимальное количество посещений (для LIMITED)", example = "8")
    @Min(value = 1, message = "Количество посещений должно быть не менее 1")
    private Integer maxVisits;

    @Schema(description = "Срок действия в днях", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "Срок действия должен быть не менее 1 дня")
    private Integer durationDays;

    @Schema(description = "Статус абонемента", example = "ACTIVE")
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
}

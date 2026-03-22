package ru.univ.grain.dto;

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
public class SubscriptionDto {

    @NotBlank(message = "Название абонемента обязательно")
    private String name;

    private String description;

    @NotNull(message = "Цена абонемента обязательна")
    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;

    @NotNull(message = "Тип абонемента обязателен")
    private SubscriptionType subscriptionType;

    @Min(value = 1, message = "Количество посещений должно быть не менее 1")
    private Integer maxVisits;

    @Min(value = 1, message = "Срок действия должен быть не менее 1 дня")
    private Integer durationDays;

    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
}

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
    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    private SubscriptionType subscriptionType;

    @Min(1)
    private Integer maxVisits;

    @Min(1)
    private Integer durationDays;

    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
}

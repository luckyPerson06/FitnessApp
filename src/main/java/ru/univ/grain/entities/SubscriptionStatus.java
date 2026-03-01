package ru.univ.grain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SubscriptionStatus {
    ACTIVE("Активен"),
    EXPIRED("Истек"),
    CANCELLED("Отменен"),
    USED("Использован");

    private String displayName;
}

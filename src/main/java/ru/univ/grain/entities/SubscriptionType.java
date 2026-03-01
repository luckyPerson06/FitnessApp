package ru.univ.grain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SubscriptionType {
    LIMITED("Лимитированный"),
    UNLIMITED("Безлимитный");

    private String displayName;
}

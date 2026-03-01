package ru.univ.grain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ClientStatus {
    ACTIVE("Активен"),
    FROZEN("Заморожен"),
    BLOCKED("Заблокирован");

    private String displayName;
}

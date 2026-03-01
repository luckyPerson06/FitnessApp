package ru.univ.grain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VisitStatus {
    BOOKED("Записан", "Клиент записался на тренировку"),
    ATTENDED("Посетил", "Клиент пришел и позанимался"),
    NO_SHOW("Не пришел", "Клиент записался, но не явился"),
    CANCELLED("Отменил", "Клиент отменил запись"),
    COMPLETED("Завершена", "Тренировка успешно проведена");

    private final String displayName;
    private final String description;

    public boolean isActive() {
        return this == BOOKED;
    }

    public boolean isCompleted() {
        return this == ATTENDED || this == COMPLETED;
    }

    public boolean isCancelled() {
        return this == NO_SHOW || this == CANCELLED;
    }
}

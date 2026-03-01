package ru.univ.grain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum WorkoutSessionStatus {
    SCHEDULED("Запланировано"),
    CONFIRMED("Подтверждено"),
    CANCELLED("Отменено"),
    COMPLETED("Проведено"),
    FULL_BOOKED("Нет мест");

    private String displayName;
}

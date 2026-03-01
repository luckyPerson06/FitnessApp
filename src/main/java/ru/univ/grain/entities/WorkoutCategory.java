package ru.univ.grain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum WorkoutCategory {
    GROUP("Групповые"),
    INDIVIDUAL("Индивидуальные"),
    SPECIAL("Спецпрограммы"),
    ALL("Все типы");

    private String displayName;
}

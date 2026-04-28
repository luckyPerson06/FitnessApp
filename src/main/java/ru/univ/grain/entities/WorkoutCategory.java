package ru.univ.grain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum WorkoutCategory {
    GROUP("Групповые"),
    MINI_GROUP("Мини группа (до 4 человек)"),
    INDIVIDUAL("Индивидуальные"),
    SPECIAL("Спецпрограммы");

    private String displayName;
}

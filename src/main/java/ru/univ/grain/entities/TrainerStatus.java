package ru.univ.grain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum TrainerStatus {
    ACTIVE("Активен"),
    VACATION("В отпуске"),
    FIRED("Уволен"),
    PROBATION("На испытательном сроке");

    private String displayName;
}

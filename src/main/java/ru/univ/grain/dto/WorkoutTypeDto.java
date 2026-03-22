package ru.univ.grain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.univ.grain.entities.WorkoutCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutTypeDto {
    @NotBlank(message = "Название типа тренировки обязательно")
    private String name;

    private String description;
    private String iconPath;
    private Boolean isActive = true;

    @NotNull(message = "Категория обязательна")
    private WorkoutCategory category;
}


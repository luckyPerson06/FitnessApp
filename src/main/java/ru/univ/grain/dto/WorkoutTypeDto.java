package ru.univ.grain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.univ.grain.entities.WorkoutCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutTypeDto {
    @NotBlank
    private String name;

    private String description;
    private String iconPath;
    private Boolean isActive = true;
    private WorkoutCategory category;
}


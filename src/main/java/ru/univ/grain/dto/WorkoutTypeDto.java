package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.univ.grain.entities.DifficultyLevel;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Тип тренировки")
public class WorkoutTypeDto {

    @Schema(description = "ID типа тренировки", example = "1")
    private Long id;

    @Schema(description = "ID тренеров для назначения")
    private List<Long> trainerIds;

    @Schema(description = "Название", example = "МЯГКАЯ СИЛА", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Название обязательно")
    private String name;

    @Schema(description = "Описание", example = "Эффективный комплекс упражнений...")
    private String description;

    @Schema(description = "Путь к иконке", example = "/icons/soft-strength.png")
    private String iconPath;

    @Schema(description = "Активен ли тип тренировки", example = "true")
    private Boolean isActive = true;

    @Schema(description = "Уровень подготовленности", example = "ALL_LEVELS")
    private DifficultyLevel difficultyLevel;

    @Schema(description = "Противопоказания", example = "Сложные формы искривления позвоночника...")
    private String contraindications;

    @Schema(description = "Преимущества направления", example = "[\"укрепление костной и мышечной тканей\", \"увеличение скорости обмена веществ\"]")
    private List<String> benefits;
}

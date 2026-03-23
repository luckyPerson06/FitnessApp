package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.univ.grain.entities.WorkoutCategory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на создание/обновление типа тренировки")
public class WorkoutTypeDto {

    @Schema(description = "Название типа тренировки", example = "Йога", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Название типа тренировки обязательно")
    private String name;

    @Schema(description = "Описание типа тренировки", example = "Хатха-йога для начинающих")
    private String description;

    @Schema(description = "Путь к иконке", example = "/icons/yoga.png")
    private String iconPath;

    @Schema(description = "Активен ли тип тренировки", example = "true")
    private Boolean isActive = true;

    @Schema(description = "Категория тренировки", example = "GROUP", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Категория обязательна")
    private WorkoutCategory category;
}

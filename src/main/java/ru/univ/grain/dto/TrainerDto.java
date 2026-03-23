package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.univ.grain.entities.TrainerStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на создание/обновление тренера")
public class TrainerDto {

    @Schema(description = "Имя тренера", example = "Анна", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Имя тренера обязательно")
    private String firstName;

    @Schema(description = "Фамилия тренера", example = "Смирнова", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Фамилия тренера обязательна")
    private String lastName;

    @Schema(description = "Путь к фото", example = "/images/trainers/smirnova.jpg")
    private String photoPath;

    @Schema(description = "Описание тренера", example = "Опытный тренер по йоге")
    private String description;

    @Schema(description = "Статус тренера", example = "ACTIVE")
    private TrainerStatus status = TrainerStatus.ACTIVE;
}

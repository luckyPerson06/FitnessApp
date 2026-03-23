package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на массовое создание тренировок")
public class WorkoutSessionBulkRequest {

    @Schema(description = "Список тренировок для создания", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Список тренировок не может быть null")
    @NotEmpty(message = "Список тренировок не может быть пустым")
    @Valid
    private List<WorkoutSessionDto> sessions;
}

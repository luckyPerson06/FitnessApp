package ru.univ.grain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Стандартный формат ошибки API")
public class ApiError {

    @Schema(description = "HTTP статус код", example = "404")
    private int status;

    @Schema(description = "Название ошибки", example = "Not Found")
    private String error;

    @Schema(description = "Сообщение об ошибке", example = "Клиент с id 20 не найден")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/clients/20")
    private String path;

    @Schema(description = "Время возникновения ошибки", example = "2026-03-22T15:24:45.454")
    private LocalDateTime timestamp;

    @Schema(description = "Детали ошибок валидации (поле -> сообщение)")
    private Map<String, String> validationErrors;
}

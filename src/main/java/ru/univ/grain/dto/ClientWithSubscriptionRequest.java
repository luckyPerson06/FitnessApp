package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Запрос на создание клиента с новым абонементом")
public class ClientWithSubscriptionRequest {

    @Schema(description = "Данные клиента", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Данные клиента обязательны")
    @Valid
    private ClientDto client;

    @Schema(description = "Данные абонемента", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Данные абонемента обязательны")
    @Valid
    private SubscriptionDto subscription;
}

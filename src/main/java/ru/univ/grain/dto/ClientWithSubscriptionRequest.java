package ru.univ.grain.dto;

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
public class ClientWithSubscriptionRequest {

    @NotNull(message = "Данные клиента обязательны")
    @Valid
    private ClientDto client;

    @NotNull(message = "Данные абонемента обязательны")
    @Valid
    private SubscriptionDto subscription;
}

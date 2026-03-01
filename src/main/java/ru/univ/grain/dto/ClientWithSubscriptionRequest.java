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

    @NotNull
    @Valid
    private ClientDto client;

    @NotNull
    @Valid
    private SubscriptionDto subscription;
}

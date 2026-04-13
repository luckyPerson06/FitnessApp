package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.univ.grain.entities.ClientStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Профиль клиента")
public class ProfileResponse {

    @Schema(description = "ID клиента")
    private Long id;

    @Schema(description = "Полное имя")
    private String fullName;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Телефон")
    private String phoneNumber;

    @Schema(description = "Статус")
    private ClientStatus status;

    @Schema(description = "Активные абонементы")
    private List<SubscriptionDto> activeSubscriptions;

    @Schema(description = "Предстоящие тренировки")
    private List<VisitDto> upcomingVisits;

    @Schema(description = "История посещений")
    private List<VisitDto> visitHistory;
}

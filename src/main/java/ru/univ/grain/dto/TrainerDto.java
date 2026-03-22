package ru.univ.grain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.univ.grain.entities.TrainerStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerDto {

    @NotBlank(message = "Имя тренера обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия тренера обязательна")
    private String lastName;

    private String photoPath;

    private String description;

    private TrainerStatus status = TrainerStatus.ACTIVE;
}

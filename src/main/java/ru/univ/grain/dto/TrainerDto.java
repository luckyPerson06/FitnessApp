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
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String photoPath;

    private String description;

    private TrainerStatus status = TrainerStatus.ACTIVE;
}

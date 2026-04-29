package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@Schema(description = "Запрос на создание клиента")
public class ClientDto extends BaseClientRequest {
}

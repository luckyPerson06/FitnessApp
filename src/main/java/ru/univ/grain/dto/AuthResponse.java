package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.univ.grain.entities.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ответ при успешной аутентификации")
public class AuthResponse {

    @Schema(description = "JWT токен для доступа к API", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Builder.Default
    @Schema(description = "Тип токена", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Email пользователя", example = "ivan@mail.com")
    private String email;

    @Schema(description = "Роль пользователя", example = "CLIENT")
    private Role role;

    @Schema(description = "Имя пользователя для отображения", example = "Иванов Иван")
    private String displayName;

    @Schema(description = "ID клиента (если роль CLIENT)", example = "1")
    private Long clientId;
}

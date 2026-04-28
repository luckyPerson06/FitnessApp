package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Информация о фитнес-клубе")
public class ClubInfoDto {

    @Schema(description = "ID записи", example = "1")
    private Long id;

    @Schema(description = "Текст о клубе", example = "СТУДИЯ ФИТНЕСА KVETKA - это пространство...")
    private String aboutText;

    @Schema(description = "Адрес", example = "г. Пинск, ул. Брестская, д.137, к.5")
    private String address;

    @Schema(description = "Телефон", example = "+375291234567")
    private String phone;

    @Schema(description = "Email клуба", example = "info@kvetka.by")
    private String email;

    @Schema(description = "Часы работы", example = "Пн-Пт: 9:00-21:00, Сб-Вс: 10:00-18:00")
    private String workingHours;

    @Schema(description = "Координаты для карты", example = "52.1234,26.5678")
    private String mapCoordinates;


    @Schema(description = "Ссылки на социальные сети",
            example = "{\"instagram\": \"https://instagram.com/kvetka\", \"telegram\": \"https://t.me/kvetka\"}")
    private Map<String, String> socialLinks;

    @Schema(description = "SEO заголовок", example = "Фитнес-студия KVETKA в Пинске")
    private String seoTitle;

    @Schema(description = "SEO описание", example = "Хатха-йога, пилатес, здоровая спина...")
    private String seoDescription;
}

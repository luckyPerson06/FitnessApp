package ru.univ.grain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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

    @Schema(description = "Путь к логотипу", example = "/images/logo.png")
    private String logoPath;

    @Schema(description = "Путь к главному изображению", example = "/images/hero.jpg")
    private String heroImagePath;

    @Schema(description = "Ссылка на Instagram", example = "https://instagram.com/kvetka")
    private String instagramUrl;

    @Schema(description = "Ссылка на Telegram", example = "https://t.me/kvetka")
    private String telegramUrl;

    @Schema(description = "Ссылка на VK", example = "https://vk.com/kvetka")
    private String vkUrl;

    @Schema(description = "WhatsApp телефон", example = "+375291234567")
    private String whatsappPhone;

    @Schema(description = "SEO заголовок", example = "Фитнес-студия KVETKA в Пинске")
    private String seoTitle;

    @Schema(description = "SEO описание", example = "Хатха-йога, пилатес, здоровая спина...")
    private String seoDescription;
}

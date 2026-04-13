package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "club_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "about_text", columnDefinition = "TEXT")
    private String aboutText;

    @Column(length = 500)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "working_hours", length = 500)
    private String workingHours;

    @Column(name = "map_coordinates", length = 100)
    private String mapCoordinates;

    @Column(name = "logo_path", length = 255)
    private String logoPath;

    @Column(name = "hero_image_path", length = 255)
    private String heroImagePath;

    // Социальные сети
    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    @Column(name = "telegram_url", length = 255)
    private String telegramUrl;

    @Column(name = "vk_url", length = 255)
    private String vkUrl;

    @Column(name = "whatsapp_phone", length = 20)
    private String whatsappPhone;

    // SEO поля
    @Column(name = "seo_title", length = 100)
    private String seoTitle;

    @Column(name = "seo_description", length = 300)
    private String seoDescription;
}

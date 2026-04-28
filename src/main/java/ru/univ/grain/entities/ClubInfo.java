package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    private Map<String, String> socialLinks = new HashMap<>();

    @Column(name = "seo_title", length = 100)
    private String seoTitle;

    @Column(name = "seo_description", length = 300)
    private String seoDescription;
}

package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workout_types",
        indexes = {
                @Index(name = "idx_workout_type_category", columnList = "category"),
                @Index(name = "idx_workout_type_active", columnList = "is_active"),
                @Index(name = "idx_workout_type_name", columnList = "name")
        })
public class WorkoutType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "icon_path", columnDefinition = "TEXT")
    private String iconPath;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private DifficultyLevel difficultyLevel;

    @Column(name = "contraindications", length = 1000)
    private String contraindications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "benefits", columnDefinition = "jsonb")
    private List<String> benefits = new ArrayList<>();

    @ManyToMany(mappedBy = "specializations", fetch = FetchType.LAZY)
    private List<Trainer> trainers;

    @ManyToMany(mappedBy = "allowedWorkoutTypes", fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "workoutType", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutSession> workoutSessions;

    @PreRemove
    private void beforeDelete() {
        for (WorkoutSession session : workoutSessions) {
            session.setWorkoutType(null);
        }
        for (Trainer trainer : trainers) {
            trainer.getSpecializations().remove(this);
        }
        for (Subscription subscription : subscriptions) {
            subscription.getAllowedWorkoutTypes().remove(this);
        }
    }
}

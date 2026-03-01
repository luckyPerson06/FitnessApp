package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(length = 500)
    private String description;

    @Column(name = "icon_path", length = 255)
    private String iconPath;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkoutCategory category;

    @ManyToMany(mappedBy = "specializations", fetch = FetchType.LAZY)
    private List<Trainer> trainers;

    @ManyToMany(mappedBy = "allowedWorkoutTypes", fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "workoutType", fetch = FetchType.LAZY)
    private List<WorkoutSession> workoutSessions;
}

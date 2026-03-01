package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "trainers",
        indexes = {
                @Index(name = "idx_trainer_status",
                        columnList = "status"),
                @Index(name = "idx_trainer_name",
                        columnList = "last_name, first_name")
        })
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "photo_path", length = 255)
    private String photoPath;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrainerStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainer_specializations",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "workout_type_id"),
            indexes = {
                    @Index(name = "idx_trainer_spec_trainer_id",
                            columnList = "trainer_id"),
                    @Index(name = "idx_trainer_spec_workout_id",
                            columnList = "workout_type_id")
            }
    )
    private Set<WorkoutType> specializations;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<WorkoutSession> workoutSessions;
}

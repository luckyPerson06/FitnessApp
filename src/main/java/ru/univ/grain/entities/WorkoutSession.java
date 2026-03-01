package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workout_sessions",
        indexes = {
                @Index(name = "idx_session_trainer",
                        columnList = "trainer_id"),
                @Index(name = "idx_session_workout",
                        columnList = "workout_type_id"),
                @Index(name = "idx_session_day",
                        columnList = "day_of_week"),
                @Index(name = "idx_session_status",
                        columnList = "status"),
                @Index(name = "idx_session_datetime",
                        columnList = "day_of_week, start_time")
        })
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_type_id", nullable = false)
    private WorkoutType workoutType;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkoutSessionStatus status;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    @OneToMany(mappedBy = "workoutSession", fetch = FetchType.LAZY,
                cascade = CascadeType.ALL)
    private List<Visit> visits;

    @Version
    private Integer version;
}

package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workout_sessions",
        indexes = {
                @Index(name = "idx_session_trainer", columnList = "trainer_id"),
                @Index(name = "idx_session_workout", columnList = "workout_type_id"),
                @Index(name = "idx_session_day", columnList = "day_of_week"),
                @Index(name = "idx_session_date", columnList = "session_date"),
                @Index(name = "idx_session_status", columnList = "status"),
                @Index(name = "idx_session_room", columnList = "room")
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

    @Column(name = "session_date")
    private LocalDate sessionDate;

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

    @Column(name = "is_recurring", nullable = false)
    private boolean isRecurring = true;

    @Column(name = "recurring_until")
    private LocalDate recurringUntil;

    @Column(name = "room", length = 50)
    private String room;

    @Builder.Default
    @OneToMany(mappedBy = "workoutSession", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visit> visits = new ArrayList<>();

    @Version
    private Integer version = 0;

    @PreRemove
    private void beforeDelete() {
        if (trainer != null) {
            trainer.getWorkoutSessions().remove(this);
        }
        if (workoutType != null) {
            workoutType.getWorkoutSessions().remove(this);
        }
    }

    public boolean matchesDate(LocalDate date) {
        if (!isRecurring && sessionDate != null) {
            return sessionDate.equals(date);
        }
        if (isRecurring) {
            final boolean dayMatches = date.getDayOfWeek() == dayOfWeek;
            final boolean beforeUntil = recurringUntil == null || !date.isAfter(recurringUntil);
            return dayMatches && beforeUntil;
        }
        return false;
    }
}

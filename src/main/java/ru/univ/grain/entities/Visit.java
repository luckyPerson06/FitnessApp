package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "visits",
        indexes = {
                @Index(name = "idx_visit_client",
                        columnList = "client_id"),
                @Index(name = "idx_visit_session",
                        columnList = "workout_session_id"),
                @Index(name = "idx_visit_subscription",
                        columnList = "subscription_id"),
                @Index(name = "idx_visit_status",
                        columnList = "status"),
                @Index(name = "idx_visit_datetime",
                        columnList = "visit_time"),
                @Index(name = "idx_visit_client_status",
                        columnList = "client_id, status"),
                @Index(name = "idx_visit_date_status",
                        columnList = "visit_time, status")
        })
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSession workoutSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "visit_time", nullable = false)
    private LocalDateTime visitTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisitStatus status;

    @Version
    private Integer version;
}

package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscription_type",
                        columnList = "subscription_type"),
                @Index(name = "idx_subscription_status",
                        columnList = "status"),
                @Index(name = "idx_subscription_price",
                        columnList = "price")
        })
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private SubscriptionType subscriptionType;

    @Column(name = "max_visits")
    private Integer maxVisits;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "subscription_workout_types",
            joinColumns = @JoinColumn(name = "subscription_id"),
            inverseJoinColumns = @JoinColumn(name = "workout_type_id"),
            indexes = {
                    @Index(name = "idx_sub_workout_sub_id",
                            columnList = "subscription_id"),
                    @Index(name = "idx_sub_workout_workout_id",
                            columnList = "workout_type_id")
            }
    )
    private List<WorkoutType> allowedWorkoutTypes;

    @ManyToMany(mappedBy = "subscriptions", fetch = FetchType.LAZY)
    private List<Client> clients;


    @PreRemove
    private void removeAssociations() {

        for (Client client : clients) {
            client.getSubscriptions().remove(this);
        }

        for (WorkoutType workoutType : allowedWorkoutTypes) {
            workoutType.getSubscriptions().remove(this);
        }
    }

}

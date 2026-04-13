package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients",
        indexes = {
                @Index(name = "idx_client_email", columnList = "email"),
                @Index(name = "idx_client_phone", columnList = "phone_number"),
                @Index(name = "idx_client_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "client_subscriptions",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "subscription_id"),
            indexes = {
                    @Index(name = "idx_client_sub_client_id", columnList = "client_id"),
                    @Index(name = "idx_client_sub_sub_id", columnList = "subscription_id")
            }
    )
    private List<Subscription> subscriptions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visit> visits = new ArrayList<>();

    @PreRemove
    private void beforeDelete() {
        for (Subscription subscription : subscriptions) {
            subscription.getClients().remove(this);
        }
    }

    public String getFullName() {
        if (middleName != null && !middleName.isEmpty()) {
            return String.format("%s %s %s", lastName, firstName, middleName);
        }
        return String.format("%s %s", lastName, firstName);
    }
}

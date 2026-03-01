package ru.univ.grain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "clients",
        indexes = {
                @Index(name = "idx_client_email",
                        columnList = "email"),
                @Index(name = "idx_client_phone",
                        columnList = "phoneNumber"),
                @Index(name = "idx_client_status",
                        columnList = "status")
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

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String middleName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(length = 15)
    private String phoneNumber;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "client_subscriptions",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "subscription_id"),
            indexes = {
                    @Index(name = "idx_client_sub_client_id",
                            columnList = "client_id"),
                    @Index(name = "idx_client_sub_sub_id",
                            columnList = "subscription_id")
            }
    )
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visit> visits;
}

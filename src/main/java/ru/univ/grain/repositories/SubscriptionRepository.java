package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.Subscription;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findBySubscriptionType(SubscriptionType type);

    Optional<Subscription> findByName(String name);
}

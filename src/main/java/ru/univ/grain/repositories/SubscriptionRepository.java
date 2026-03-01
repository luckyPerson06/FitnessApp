package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.Subscription;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findBySubscriptionType(SubscriptionType type);

    @Query("SELECT s FROM Subscription s JOIN s.allowedWorkoutTypes wt WHERE wt.id = :workoutTypeId")
    List<Subscription> findByAllowedWorkoutTypeId(@Param("workoutTypeId") Long workoutTypeId);

    Subscription findByName(String name);
}

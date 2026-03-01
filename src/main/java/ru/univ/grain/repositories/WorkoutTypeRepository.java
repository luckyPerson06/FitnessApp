package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.WorkoutCategory;
import ru.univ.grain.entities.WorkoutType;

import java.util.List;

@Repository
public interface WorkoutTypeRepository extends JpaRepository<WorkoutType, Long> {

    WorkoutType findByNameIgnoreCase(String name);

    List<WorkoutType> findByIsActiveTrue();

    List<WorkoutType> findByCategory(WorkoutCategory category);

    @Query("SELECT wt FROM WorkoutType wt JOIN wt.trainers t WHERE t.id = :trainerId")
    List<WorkoutType> findByTrainerId(@Param("trainerId") Long trainerId);

    @Query("SELECT wt FROM WorkoutType wt JOIN wt.subscriptions s WHERE s.id = :subscriptionId")
    List<WorkoutType> findBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    boolean existsByNameIgnoreCase(String name);
}

package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.WorkoutType;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutTypeRepository extends JpaRepository<WorkoutType, Long> {

    Optional<WorkoutType> findByNameIgnoreCase(String name);

    List<WorkoutType> findByIsActiveTrue();

    @Query("SELECT wt FROM WorkoutType wt JOIN wt.trainers t WHERE t.id = :trainerId")
    List<WorkoutType> findByTrainerId(@Param("trainerId") Long trainerId);

    boolean existsByNameIgnoreCase(String name);
}

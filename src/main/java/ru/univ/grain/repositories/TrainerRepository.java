package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.Trainer;
import ru.univ.grain.entities.TrainerStatus;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    List<Trainer> findByStatus(TrainerStatus status);

    @Query("SELECT t FROM Trainer t JOIN t.specializations s WHERE s.name = :specializationName")
    List<Trainer> findBySpecializationName(@Param("specializationName") String specializationName);

    @Query("SELECT DISTINCT t FROM Trainer t JOIN t.workoutSessions ws WHERE ws.dayOfWeek = :dayOfWeek")
    List<Trainer> findTrainersWithSessionOnDay(@Param("dayOfWeek") DayOfWeek dayOfWeek);

    List<Trainer> findByStatusIn(List<TrainerStatus> statuses);

    @Query("SELECT DISTINCT t FROM Trainer t LEFT JOIN FETCH t.specializations LEFT JOIN FETCH t.workoutSessions")
    List<Trainer> findAllWithDetails();
}

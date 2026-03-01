package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.WorkoutSession;
import ru.univ.grain.entities.WorkoutSessionStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByTrainerId(Long trainerId);

    List<WorkoutSession> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<WorkoutSession> findByWorkoutTypeId(Long workoutTypeId);

    List<WorkoutSession> findByStatus(WorkoutSessionStatus status);

    List<WorkoutSession> findByDayOfWeekAndStatus(DayOfWeek dayOfWeek, WorkoutSessionStatus status);

    @Query("SELECT s FROM WorkoutSession s WHERE s.trainer.id = :trainerId " +
            "AND s.dayOfWeek = :dayOfWeek " +
            "AND s.status IN ('SCHEDULED', 'CONFIRMED') " +  // ACTIVE заменен на SCHEDULED, CONFIRMED
            "AND ((s.startTime BETWEEN :start AND :end) OR (s.endTime BETWEEN :start AND :end))")
    List<WorkoutSession> findOverlappingSessions(
            @Param("trainerId") Long trainerId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end);

    @Query("SELECT s FROM WorkoutSession s WHERE s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime <= :time AND s.endTime >= :time")
    List<WorkoutSession> findByTime(@Param("dayOfWeek") DayOfWeek dayOfWeek, @Param("time") LocalTime time);

    @Query("SELECT s FROM WorkoutSession s WHERE s.status = 'SCHEDULED' ORDER BY s.dayOfWeek, s.startTime")
    List<WorkoutSession> findAllScheduled();
}

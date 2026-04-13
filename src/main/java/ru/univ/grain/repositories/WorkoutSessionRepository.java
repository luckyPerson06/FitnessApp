package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.WorkoutSession;
import ru.univ.grain.entities.WorkoutSessionStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByTrainerId(Long trainerId);

    List<WorkoutSession> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<WorkoutSession> findByWorkoutTypeId(Long workoutTypeId);

    List<WorkoutSession> findByStatus(WorkoutSessionStatus status);

    List<WorkoutSession> findByDayOfWeekAndStatus(DayOfWeek dayOfWeek, WorkoutSessionStatus status);

    List<WorkoutSession> findBySessionDate(LocalDate sessionDate);

    List<WorkoutSession> findBySessionDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT ws FROM WorkoutSession ws WHERE " +
            "(ws.isRecurring = true AND ws.dayOfWeek = :dayOfWeek AND " +
            "(ws.recurringUntil IS NULL OR ws.recurringUntil >= :date)) " +
            "OR (ws.isRecurring = false AND ws.sessionDate = :date)")
    List<WorkoutSession> findSessionsForDate(@Param("date") LocalDate date,
                                             @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT s FROM WorkoutSession s WHERE s.trainer.id = :trainerId " +
            "AND s.dayOfWeek = :dayOfWeek " +
            "AND s.status IN ('SCHEDULED', 'CONFIRMED') " +
            "AND ((s.startTime BETWEEN :start AND :end) OR (s.endTime BETWEEN :start AND :end))")
    List<WorkoutSession> findOverlappingSessions(
            @Param("trainerId") Long trainerId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end);

    @Query("SELECT s FROM WorkoutSession s WHERE s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime <= :time AND s.endTime >= :time")
    List<WorkoutSession> findByTime(@Param("dayOfWeek") DayOfWeek dayOfWeek,
                                    @Param("time") LocalTime time);

    @Query("SELECT ws FROM WorkoutSession ws " +
            "WHERE ws.trainer.id = :trainerId " +
            "AND ws.dayOfWeek = :dayOfWeek " +
            "AND ((ws.startTime BETWEEN :start AND :end) OR (ws.endTime BETWEEN :start AND :end))")
    List<WorkoutSession> findOverlappingSessionsForTrainer(
            @Param("trainerId") Long trainerId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end);
}

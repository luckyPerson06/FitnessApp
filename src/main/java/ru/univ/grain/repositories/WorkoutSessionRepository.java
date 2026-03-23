package ru.univ.grain.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            "AND s.status IN ('SCHEDULED', 'CONFIRMED') " +
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

    @Query("SELECT ws FROM WorkoutSession ws " +
            "WHERE LOWER(ws.trainer.lastName) LIKE LOWER(CONCAT('%', :trainerLastName, '%')) " +
            "AND ws.dayOfWeek = :dayOfWeek")
    Page<WorkoutSession> findByTrainerLastNameAndDay(
            @Param("trainerLastName") String trainerLastName,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            Pageable pageable
    );

    @Query(value = "SELECT ws.* FROM workout_sessions ws " +
            "JOIN trainers t ON ws.trainer_id = t.id " +
            "WHERE LOWER(t.last_name) LIKE LOWER(CONCAT('%', :trainerLastName, '%')) " +
            "AND ws.day_of_week = :dayOfWeek",
            countQuery = "SELECT COUNT(*) FROM workout_sessions ws " +
                    "JOIN trainers t ON ws.trainer_id = t.id " +
                    "WHERE LOWER(t.last_name) LIKE LOWER(CONCAT('%', :trainerLastName, '%')) " +
                    "AND ws.day_of_week = :dayOfWeek",
            nativeQuery = true)
    Page<WorkoutSession> findByTrainerLastNameAndDayNative(
            @Param("trainerLastName") String trainerLastName,
            @Param("dayOfWeek") String dayOfWeek,
            Pageable pageable
    );

    @Query("SELECT ws FROM WorkoutSession ws " +
            "WHERE ws.trainer.id = :trainerId " +
            "AND ws.dayOfWeek = :dayOfWeek " +
            "AND ((ws.startTime BETWEEN :start AND :end) OR (ws.endTime BETWEEN :start AND :end))")
    List<WorkoutSession> findOverlappingSessionsForTrainer(
            @Param("trainerId") Long trainerId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end
    );

}

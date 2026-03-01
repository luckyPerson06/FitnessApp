package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.Visit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByClientId(Long clientId);

    List<Visit> findByWorkoutSessionId(Long workoutSessionId);

    List<Visit> findByVisitTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.subscription.id = :subscriptionId AND v.status = 'ATTENDED'")
    long countAttendedBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    @Query("SELECT HOUR(v.visitTime), COUNT(v) FROM Visit v " +
            "WHERE v.status = 'ATTENDED' " +
            "GROUP BY HOUR(v.visitTime)")
    List<Object[]> getVisitsByHour();

    @Query("SELECT v FROM Visit v WHERE v.client.id = :clientId AND v.status = 'BOOKED'")
    List<Visit> findBookedVisitsByClient(@Param("clientId") Long clientId);

    @Query("SELECT v FROM Visit v WHERE v.workoutSession.id = :sessionId AND v.status = 'BOOKED'")
    List<Visit> findBookedVisitsBySession(@Param("sessionId") Long sessionId);
}

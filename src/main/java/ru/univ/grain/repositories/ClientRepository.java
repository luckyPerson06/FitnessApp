package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.Client;
import ru.univ.grain.entities.ClientStatus;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByLastNameIgnoreCase(String lastName);

    Client findByEmail(String lastName);

    List<Client> findByStatus(ClientStatus status);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT c FROM Client c JOIN c.subscriptions s WHERE s.status = 'ACTIVE'")
    List<Client> findClientsWithActiveSubscriptions();

    @Query("SELECT v.client FROM Visit v WHERE v.workoutSession.id = :sessionId AND v.status = 'BOOKED'")
    List<Client> findBookedClientsBySession(@Param("sessionId") Long sessionId);

}

package ru.univ.grain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.univ.grain.entities.Client;
import ru.univ.grain.entities.ClientStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByLastNameIgnoreCase(String lastName);

    Optional<Client> findByEmail(String email);

    List<Client> findByStatus(ClientStatus status);

    boolean existsByEmail(String email);
}

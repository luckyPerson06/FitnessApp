package ru.univ.grain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.univ.grain.domain.Client;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findFirstById(long id);
    List<Client> findByLastNameIgnoreCase(String lastName);

}

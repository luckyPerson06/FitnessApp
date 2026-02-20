package ru.univ.grain.repository;

import org.springframework.stereotype.Repository;
import ru.univ.grain.domain.Client;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ClientRepository {
    private final List<Client> storage = new ArrayList<>();
    private long idSequence = 0;

    public Client save(Client client) {
        client.setId(idSequence++);
        storage.add(client);
        return client;
    }

    public Client findById(long id) {
        return storage.stream()
                .filter(client -> client.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }


    public List<Client> findByLastName(String lastName) {
        return storage.stream()
                .filter(client -> client.getLastName().equalsIgnoreCase(lastName))
                .toList();
    }

    public void deleteById(long id) {
        storage.removeIf(client -> client.getId() == id);
    }

}

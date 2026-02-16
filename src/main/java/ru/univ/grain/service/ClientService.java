package ru.univ.grain.service;

import org.springframework.stereotype.Service;

import ru.univ.grain.domain.Client;
import ru.univ.grain.repository.ClientRepository;

import java.util.List;


@Service
public class ClientService {
    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public Client createClient(Client client) {
        return repository.save(client);
    }

    public Client getClientById(long id) {
        return repository.findById(id);
    }

    public List<Client> findByLastName(String lastName) {
        return repository.findByLastName(lastName);
    }

}

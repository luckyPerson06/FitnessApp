package ru.univ.grain.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import ru.univ.grain.domain.Client;
import ru.univ.grain.dto.ClientUpdateDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.repository.ClientRepository;

import java.util.List;


@Service
@AllArgsConstructor
public class ClientService {
    private final ClientRepository repository;

    public Client createClient(Client client) {
        return repository.save(client);
    }

    public Client updateClient(long id, ClientUpdateDto dto) {
        final Client client = repository.findById(id);

        client.setFirstName(dto.getFirstName());
        client.setMiddleName(dto.getMiddleName());
        client.setLastName(dto.getLastName());
        client.setPhoneNumber(dto.getPhoneNumber());
        client.setEmail(dto.getEmail());
        client.setStatus(dto.getStatus());

        return client;
    }

    public Client patch(long id, ClientPatchDto dto) {

        final Client client = getClientById(id);

        if (dto.getFirstName() != null) {
            client.setFirstName(dto.getFirstName());
        }
        if (dto.getMiddleName() != null) {
            client.setMiddleName(dto.getMiddleName());
        }
        if (dto.getLastName() != null) {
            client.setLastName(dto.getLastName());
        }
        if (dto.getPhoneNumber() != null) {
            client.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getEmail() != null) {
            client.setEmail(dto.getEmail());
        }

        return client;
    }

    public Client getClientById(long id) {
        return repository.findById(id);
    }

    public List<Client> getClientByLastName(String lastName) {
        return repository.findByLastName(lastName);
    }


    public void deleteClientById(long id) {
        repository.deleteById(id);
    }

}

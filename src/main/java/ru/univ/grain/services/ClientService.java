package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.dto.ClientDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.entities.Client;
import ru.univ.grain.entities.ClientStatus;
import ru.univ.grain.entities.Subscription;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.ClientMapper;
import ru.univ.grain.repositories.ClientRepository;
import ru.univ.grain.repositories.SubscriptionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ClientMapper clientMapper;

    private static final String CLIENT_NOT_FOUND = "Клиент с id %d не найден";
    private static final String CLIENT_EMAIL_NOT_FOUND = "Клиент с email %s не найден";
    private static final String DUPLICATE_EMAIL = "Клиент с email %s уже существует";
    private static final String SUBSCRIPTION_ALREADY_EXISTS = "Клиент уже имеет этот абонемент";
    private static final String SUBSCRIPTION_NOT_OWNED = "У клиента нет абонемента с id %d";
    private static final String SUBSCRIPTION_NOT_FOUND = "Абонемент с id %d не найден";

    @Transactional
    public ClientResponseDto createClient(ClientDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException(String.format(DUPLICATE_EMAIL, dto.getEmail()));
        }

        final Client client = clientMapper.toEntity(dto);
        final Client savedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(savedClient);
    }

    @Transactional
    public ClientResponseDto updateClient(Long id, ClientPatchDto dto) {
        final Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, id)));

        if (dto.getEmail() != null &&
                !client.getEmail().equals(dto.getEmail()) &&
                clientRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException(String.format(DUPLICATE_EMAIL, dto.getEmail()));
        }

        clientMapper.updateEntity(dto, client);
        final Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(updatedClient);
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getClientById(Long id) {
        final Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, id)));
        return clientMapper.toResponseDto(client);
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getClientByEmail(String email) {
        final Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_EMAIL_NOT_FOUND, email)));
        return clientMapper.toResponseDto(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getClientsByLastName(String lastName) {
        return clientRepository.findByLastNameIgnoreCase(lastName).stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getClientsByStatus(ClientStatus status) {
        return clientRepository.findByStatus(status).stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return clientRepository.existsByEmail(email);
    }

    @Transactional
    public void deleteClient(Long id) {
        final Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, id)));
        clientRepository.delete(client);
    }

    @Transactional
    public ClientResponseDto addSubscriptionToClient(Long clientId, Long subscriptionId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, clientId)));

        final Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SUBSCRIPTION_NOT_FOUND, subscriptionId)));

        if (client.getSubscriptions().contains(subscription)) {
            throw new BusinessException(SUBSCRIPTION_ALREADY_EXISTS);
        }

        client.getSubscriptions().add(subscription);
        final Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(updatedClient);
    }

    @Transactional
    public ClientResponseDto removeSubscriptionFromClient(Long clientId, Long subscriptionId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, clientId)));

        final boolean removed = client.getSubscriptions().removeIf(sub -> sub.getId().equals(subscriptionId));

        if (!removed) {
            throw new ResourceNotFoundException(String.format(SUBSCRIPTION_NOT_OWNED, subscriptionId));
        }

        final Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(updatedClient);
    }
}

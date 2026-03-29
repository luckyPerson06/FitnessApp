package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.*;
import ru.univ.grain.dto.ClientDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.mapper.ClientMapper;
import ru.univ.grain.mapper.SubscriptionMapper;
import ru.univ.grain.repositories.ClientRepository;
import ru.univ.grain.repositories.SubscriptionRepository;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ClientMapper clientMapper;
    private final SubscriptionMapper subscriptionMapper;

    private static final String CLIENT_NOT_FOUND = "Клиент с id %d не найден";
    private static final String CLIENT_EMAIL_NOT_FOUND = "Клиент с email %s не найден";
    private static final String DUPLICATE_EMAIL = "Клиент с email %s уже существует";
    private static final String SUBSCRIPTION_ALREADY_EXISTS = "Клиент уже имеет этот абонемент";
    private static final String SUBSCRIPTION_NOT_OWNED = "У клиента нет абонемента с id %d";

    @Transactional
    public ClientResponseDto createClient(final ClientDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException(String.format(DUPLICATE_EMAIL, dto.getEmail()));
        }

        final Client client = clientMapper.toEntity(dto);
        final Client savedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(savedClient);
    }

    @Transactional
    public ClientResponseDto updateClient(final Long id, final ClientPatchDto dto) {
        final Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isEmpty()) {
            throw new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, id));
        }
        final Client client = optionalClient.get();

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
    public ClientResponseDto getClientResponseById(final Long id) {
        final Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, id)));
        return clientMapper.toResponseDto(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getClientsByLastName(final String lastName) {
        return clientRepository.findByLastNameIgnoreCase(lastName).stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void deleteClientById(final Long id) {
        final Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, id)));

        clientRepository.delete(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getClientByEmail(final String email) {
        return clientRepository.findByEmail(email)
                .map(clientMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_EMAIL_NOT_FOUND, email)));
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getClientsByStatus(final ClientStatus status) {
        return clientRepository.findByStatus(status).stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getClientsWithActiveSubscriptions() {
        return clientRepository.findClientsWithActiveSubscriptions().stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getBookedClientsForSession(final Long sessionId) {
        return clientRepository.findBookedClientsBySession(sessionId).stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(final String email) {
        return clientRepository.existsByEmail(email);
    }

    public ClientResponseDto addSubscriptionToClient(final Long clientId, final Long subscriptionId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, clientId)));

        final Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Абонемент с id " + subscriptionId + " не найден"));

        if (!client.getSubscriptions().contains(subscription)) {
            client.getSubscriptions().add(subscription);
        } else {
            throw new BusinessException(SUBSCRIPTION_ALREADY_EXISTS);
        }

        final Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(updatedClient);
    }

    @Transactional
    public ClientResponseDto removeSubscriptionFromClient(final Long clientId, final Long subscriptionId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, clientId)));

        final boolean removed = client.getSubscriptions().removeIf(sub -> sub.getId().equals(subscriptionId));

        if (!removed) {
            throw new ResourceNotFoundException(String.format(SUBSCRIPTION_NOT_OWNED, subscriptionId));
        }

        final Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(updatedClient);
    }


    @Transactional
    public ClientResponseDto createClientWithNewSubscription(ClientDto clientDto, SubscriptionDto subscriptionDto) {
        if (clientRepository.existsByEmail(clientDto.getEmail())) {
            throw new DuplicateResourceException(String.format(DUPLICATE_EMAIL, clientDto.getEmail()));
        }

        subscriptionRepository.findByName(subscriptionDto.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Абонемент с названием '" + subscriptionDto.getName() + "' уже существует");
                });

        final Client client = clientMapper.toEntity(clientDto);
        client.setSubscriptions(new ArrayList<>());

        final Client savedClient = clientRepository.save(client);

        final Subscription subscription = subscriptionMapper.toEntity(subscriptionDto);
        final Subscription savedSubscription = subscriptionRepository.save(subscription);

        savedClient.getSubscriptions().add(savedSubscription);
        clientRepository.save(savedClient);

        return clientMapper.toResponseDto(savedClient);
    }



}

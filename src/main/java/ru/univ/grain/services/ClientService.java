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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ClientMapper clientMapper;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    public ClientResponseDto createClient(final ClientDto dto) {
        if (clientRepository.existsByEmail(dto.getEmail())) {
            return null;
        }

        final Client client = clientMapper.toEntity(dto);
        final Client savedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(savedClient);
    }

    @Transactional
    public ClientResponseDto updateClient(final Long id, final ClientPatchDto dto) {
        final Client client = clientRepository.findById(id).orElse(null);
        if (client == null) {
            return null;
        }

        if (dto.getEmail() != null &&
                !client.getEmail().equals(dto.getEmail()) &&
                clientRepository.existsByEmail(dto.getEmail())) {
            return null;
        }

        clientMapper.updateEntity(dto, client);
        final Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(updatedClient);
    }


    @Transactional(readOnly = true)
    public ClientResponseDto getClientResponseById(final Long id) {
        final Client client = clientRepository.findById(id).orElse(null);
        if (client == null) {
            return null;
        }
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
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponseDto getClientByEmail(final String email) {
        final Client client = clientRepository.findByEmail(email);
        if (client == null) {
            return null;
        }
        return clientMapper.toResponseDto(client);
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

    @Transactional
    public ClientResponseDto addSubscriptionToClient(final Long clientId, final Long subscriptionId) {
        final Client client = clientRepository.findById(clientId).orElse(null);
        final Subscription subscription = subscriptionRepository
                .findById(subscriptionId).orElse(null);

        if (client == null || subscription == null) {
            return null;
        }

        if (!client.getSubscriptions().contains(subscription)) {
            client.getSubscriptions().add(subscription);
        }

        final Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(updatedClient);
    }

    @Transactional
    public ClientResponseDto removeSubscriptionFromClient(final Long clientId, final Long subscriptionId) {
        final Client client = clientRepository.findById(clientId).orElse(null);
        if (client == null) {
            return null;
        }

        client.getSubscriptions().removeIf(sub -> sub.getId().equals(subscriptionId));

        final Client updatedClient = clientRepository.save(client);
        return clientMapper.toResponseDto(updatedClient);
    }


    @Transactional
    public ClientResponseDto createClientWithNewSubscription(ClientDto clientDto, SubscriptionDto subscriptionDto) {
        if (clientRepository.existsByEmail(clientDto.getEmail())) {
            return null;
        }

        if (subscriptionRepository.findByName(subscriptionDto.getName()) != null) {
            return null;
        }

        final Client client = clientMapper.toEntity(clientDto);
        if (client.getSubscriptions() == null) {
            client.setSubscriptions(new ArrayList<>());
        }

        final Client savedClient = clientRepository.save(client);

        final Subscription subscription = subscriptionMapper.toEntity(subscriptionDto);
        final Subscription savedSubscription = subscriptionRepository.save(subscription);

        savedClient.getSubscriptions().add(savedSubscription);
        clientRepository.save(savedClient);

        return clientMapper.toResponseDto(savedClient);
    }

    public ClientResponseDto createClientWithNewSubscriptionSequential(ClientDto clientDto, SubscriptionDto subscriptionDto) {

        if (clientRepository.existsByEmail(clientDto.getEmail())) {
            return null;
        }


        if (clientDto.getFirstName() == null || clientDto.getFirstName().isBlank() ||
                clientDto.getLastName() == null || clientDto.getLastName().isBlank() ||
                clientDto.getEmail() == null || clientDto.getEmail().isBlank() ||
                clientDto.getPhoneNumber() == null || clientDto.getPhoneNumber().isBlank() ||
                clientDto.getPassword() == null || clientDto.getPassword().isBlank()) {
            return null;
        }

        final Client client = clientMapper.toEntity(clientDto);
        if (client.getSubscriptions() == null) {
            client.setSubscriptions(new ArrayList<>());
        }
        final Client savedClient = clientRepository.save(client);

        if (subscriptionRepository.findByName(subscriptionDto.getName()) != null) {
            return clientMapper.toResponseDto(savedClient);
        }

        if (subscriptionDto.getName() == null || subscriptionDto.getName().isBlank() ||
                subscriptionDto.getPrice() == null ||
                subscriptionDto.getSubscriptionType() == null ||
                subscriptionDto.getDurationDays() == null) {
            return clientMapper.toResponseDto(savedClient);
        }

        if (subscriptionDto.getSubscriptionType() == SubscriptionType.LIMITED &&
                (subscriptionDto.getMaxVisits() == null || subscriptionDto.getMaxVisits() <= 0)) {
            return clientMapper.toResponseDto(savedClient);
        }

        if (subscriptionDto.getSubscriptionType() == SubscriptionType.UNLIMITED &&
                subscriptionDto.getMaxVisits() != null) {
            return clientMapper.toResponseDto(savedClient);
        }

        final Subscription subscription = subscriptionMapper.toEntity(subscriptionDto);
        final Subscription savedSubscription = subscriptionRepository.save(subscription); // АБОНЕМЕНТ В БД!

        savedClient.getSubscriptions().add(savedSubscription);
        clientRepository.save(savedClient);

        return clientMapper.toResponseDto(savedClient);
    }


}

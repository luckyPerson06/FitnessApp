package ru.univ.grain.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.univ.grain.dto.ClientDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.Client;
import ru.univ.grain.entities.ClientStatus;
import ru.univ.grain.entities.Subscription;
import ru.univ.grain.entities.SubscriptionType;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.ClientMapper;
import ru.univ.grain.mapper.SubscriptionMapper;
import ru.univ.grain.repositories.ClientRepository;
import ru.univ.grain.repositories.SubscriptionRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private ClientService clientService;




    @Test
    void createClient_ShouldReturnClient_WhenEmailIsUnique() {
        ClientDto dto = new ClientDto();
        dto.setEmail("test@mail.com");
        dto.setFirstName("Иван");
        dto.setLastName("Иванов");

        Client client = new Client();
        client.setId(1L);
        client.setEmail(dto.getEmail());

        ClientResponseDto responseDto = new ClientResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail(dto.getEmail());

        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(clientMapper.toEntity(dto)).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toResponseDto(client)).thenReturn(responseDto);

        ClientResponseDto result = clientService.createClient(dto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createClient_ShouldThrowException_WhenEmailAlreadyExists() {
        ClientDto dto = new ClientDto();
        dto.setEmail("existing@mail.com");

        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> clientService.createClient(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void getClientResponseById_ShouldReturnClient_WhenExists() {
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);
        client.setEmail("test@mail.com");

        ClientResponseDto responseDto = new ClientResponseDto();
        responseDto.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientMapper.toResponseDto(client)).thenReturn(responseDto);

        ClientResponseDto result = clientService.getClientResponseById(clientId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(clientId);
    }

    @Test
    void getClientResponseById_ShouldThrowException_WhenNotFound() {
        Long clientId = 999L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientResponseById(clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getClientByEmail_ShouldReturnClient_WhenExists() {
        String email = "test@mail.com";
        Client client = new Client();
        client.setId(1L);
        client.setEmail(email);

        ClientResponseDto responseDto = new ClientResponseDto();
        responseDto.setId(1L);
        responseDto.setEmail(email);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(clientMapper.toResponseDto(client)).thenReturn(responseDto);

        ClientResponseDto result = clientService.getClientByEmail(email);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void getClientByEmail_ShouldThrowException_WhenNotFound() {
        String email = "notfound@mail.com";

        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void updateClient_ShouldUpdateClient_WhenValid() {
        Long clientId = 1L;
        ClientPatchDto patchDto = new ClientPatchDto();
        patchDto.setFirstName("Анна");
        patchDto.setPhoneNumber("+79991234567");

        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setEmail("ivan@mail.com");

        Client updatedClient = new Client();
        updatedClient.setId(clientId);
        updatedClient.setFirstName("Анна");

        ClientResponseDto responseDto = new ClientResponseDto();
        responseDto.setId(clientId);
        responseDto.setFullName("Иванова Анна");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);
        when(clientMapper.toResponseDto(updatedClient)).thenReturn(responseDto);

        ClientResponseDto result = clientService.updateClient(clientId, patchDto);

        assertThat(result).isNotNull();
        verify(clientMapper).updateEntity(patchDto, existingClient);
        verify(clientRepository).save(existingClient);
    }

    @Test
    void updateClient_ShouldThrowException_WhenEmailConflict() {
        Long clientId = 1L;
        ClientPatchDto patchDto = new ClientPatchDto();
        patchDto.setEmail("new@mail.com");

        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setEmail("old@mail.com");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.existsByEmail("new@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> clientService.updateClient(clientId, patchDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClientById_ShouldDeleteClient_WhenExists() {
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        clientService.deleteClientById(clientId);

        verify(clientRepository).delete(client);
    }

    @Test
    void deleteClientById_ShouldThrowException_WhenNotFound() {
        Long clientId = 999L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.deleteClientById(clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(clientRepository, never()).delete(any());
    }

    @Test
    void getAllClients_ShouldReturnListOfClients() {
        Client client1 = new Client();
        client1.setId(1L);
        Client client2 = new Client();
        client2.setId(2L);

        ClientResponseDto response1 = new ClientResponseDto();
        response1.setId(1L);
        ClientResponseDto response2 = new ClientResponseDto();
        response2.setId(2L);

        when(clientRepository.findAll()).thenReturn(List.of(client1, client2));
        when(clientMapper.toResponseDto(client1)).thenReturn(response1);
        when(clientMapper.toResponseDto(client2)).thenReturn(response2);

        List<ClientResponseDto> result = clientService.getAllClients();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getClientsByStatus_ShouldReturnFilteredClients() {
        ClientStatus status = ClientStatus.ACTIVE;
        Client client = new Client();
        client.setId(1L);
        client.setStatus(status);

        ClientResponseDto response = new ClientResponseDto();
        response.setId(1L);
        response.setStatus(status);

        when(clientRepository.findByStatus(status)).thenReturn(List.of(client));
        when(clientMapper.toResponseDto(client)).thenReturn(response);

        List<ClientResponseDto> result = clientService.getClientsByStatus(status);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(status);
    }

    @Test
    void addSubscriptionToClient_ShouldAddSubscription_WhenNotExists() {
        Long clientId = 1L;
        Long subscriptionId = 1L;

        Client client = new Client();
        client.setId(clientId);
        client.setSubscriptions(new ArrayList<>());

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);

        ClientResponseDto responseDto = new ClientResponseDto();
        responseDto.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toResponseDto(client)).thenReturn(responseDto);

        ClientResponseDto result = clientService.addSubscriptionToClient(clientId, subscriptionId);

        assertThat(result).isNotNull();
        assertThat(client.getSubscriptions()).contains(subscription);
        verify(clientRepository).save(client);
    }

    @Test
    void addSubscriptionToClient_ShouldThrowException_WhenAlreadyHasSubscription() {
        Long clientId = 1L;
        Long subscriptionId = 1L;

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);

        Client client = new Client();
        client.setId(clientId);
        client.setSubscriptions(new ArrayList<>());
        client.getSubscriptions().add(subscription);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> clientService.addSubscriptionToClient(clientId, subscriptionId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже имеет");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void getClientsByLastName_ShouldReturnList() {
        String lastName = "Иванов";
        Client client = new Client();
        client.setId(1L);
        client.setLastName(lastName);

        ClientResponseDto response = new ClientResponseDto();
        response.setId(1L);

        when(clientRepository.findByLastNameIgnoreCase(lastName)).thenReturn(List.of(client));
        when(clientMapper.toResponseDto(client)).thenReturn(response);

        List<ClientResponseDto> result = clientService.getClientsByLastName(lastName);

        assertThat(result).hasSize(1);
    }

    @Test
    void removeSubscriptionFromClient_ShouldRemove_WhenExists() {
        Long clientId = 1L;
        Long subscriptionId = 1L;

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);

        Client client = new Client();
        client.setId(clientId);
        client.setSubscriptions(new ArrayList<>());
        client.getSubscriptions().add(subscription);

        ClientResponseDto response = new ClientResponseDto();
        response.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toResponseDto(client)).thenReturn(response);

        ClientResponseDto result = clientService.removeSubscriptionFromClient(clientId, subscriptionId);

        assertThat(result).isNotNull();
        assertThat(client.getSubscriptions()).doesNotContain(subscription);
        verify(clientRepository).save(client);
    }

    @Test
    void removeSubscriptionFromClient_ShouldThrowException_WhenNotExists() {
        Long clientId = 1L;
        Long subscriptionId = 999L;

        Client client = new Client();
        client.setId(clientId);
        client.setSubscriptions(new ArrayList<>());

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> clientService.removeSubscriptionFromClient(clientId, subscriptionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("нет абонемента");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void getClientsWithActiveSubscriptions_ShouldReturnList() {
        Client client = new Client();
        client.setId(1L);

        ClientResponseDto response = new ClientResponseDto();
        response.setId(1L);

        when(clientRepository.findClientsWithActiveSubscriptions()).thenReturn(List.of(client));
        when(clientMapper.toResponseDto(client)).thenReturn(response);

        List<ClientResponseDto> result = clientService.getClientsWithActiveSubscriptions();

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookedClientsForSession_ShouldReturnList() {
        Long sessionId = 1L;
        Client client = new Client();
        client.setId(1L);

        ClientResponseDto response = new ClientResponseDto();
        response.setId(1L);

        when(clientRepository.findBookedClientsBySession(sessionId)).thenReturn(List.of(client));
        when(clientMapper.toResponseDto(client)).thenReturn(response);

        List<ClientResponseDto> result = clientService.getBookedClientsForSession(sessionId);

        assertThat(result).hasSize(1);
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        String email = "test@mail.com";

        when(clientRepository.existsByEmail(email)).thenReturn(true);

        boolean result = clientService.existsByEmail(email);

        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenNotExists() {
        String email = "notfound@mail.com";

        when(clientRepository.existsByEmail(email)).thenReturn(false);

        boolean result = clientService.existsByEmail(email);

        assertThat(result).isFalse();
    }

    @Test
    void createClientWithNewSubscription_ShouldCreateBoth_WhenValid() {
        ClientDto clientDto = new ClientDto();
        clientDto.setEmail("new@mail.com");
        clientDto.setFirstName("Иван");
        clientDto.setLastName("Иванов");

        SubscriptionDto subscriptionDto = new SubscriptionDto();
        subscriptionDto.setName("Новый абонемент");
        subscriptionDto.setPrice(BigDecimal.valueOf(3000));
        subscriptionDto.setSubscriptionType(SubscriptionType.LIMITED);
        subscriptionDto.setMaxVisits(8);
        subscriptionDto.setDurationDays(30);

        Client client = new Client();
        client.setId(1L);

        Subscription subscription = new Subscription();
        subscription.setId(1L);

        ClientResponseDto response = new ClientResponseDto();
        response.setId(1L);

        when(clientRepository.existsByEmail(clientDto.getEmail())).thenReturn(false);
        when(subscriptionRepository.findByName(subscriptionDto.getName())).thenReturn(Optional.empty());
        when(clientMapper.toEntity(clientDto)).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(subscriptionMapper.toEntity(subscriptionDto)).thenReturn(subscription);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(clientMapper.toResponseDto(client)).thenReturn(response);

        ClientResponseDto result = clientService.createClientWithNewSubscription(clientDto, subscriptionDto);

        assertThat(result).isNotNull();
        verify(clientRepository, times(2)).save(any(Client.class));
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void createClientWithNewSubscription_ShouldThrowException_WhenEmailExists() {
        ClientDto clientDto = new ClientDto();
        clientDto.setEmail("existing@mail.com");

        SubscriptionDto subscriptionDto = new SubscriptionDto();

        when(clientRepository.existsByEmail(clientDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> clientService.createClientWithNewSubscription(clientDto, subscriptionDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");

        verify(clientRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void addSubscriptionToClient_ShouldThrowException_WhenClientNotFound() {
        Long clientId = 999L;
        Long subscriptionId = 1L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.addSubscriptionToClient(clientId, subscriptionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void addSubscriptionToClient_ShouldThrowException_WhenSubscriptionNotFound() {
        Long clientId = 1L;
        Long subscriptionId = 999L;

        Client client = new Client();
        client.setId(clientId);
        client.setSubscriptions(new ArrayList<>());

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.addSubscriptionToClient(clientId, subscriptionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void getClientsByLastName_ShouldReturnEmptyList_WhenNoClients() {
        String lastName = "НесуществующаяФамилия";

        when(clientRepository.findByLastNameIgnoreCase(lastName)).thenReturn(List.of());

        List<ClientResponseDto> result = clientService.getClientsByLastName(lastName);

        assertThat(result).isEmpty();
    }

    @Test
    void getClientsByStatus_ShouldReturnEmptyList_WhenNoClients() {
        ClientStatus status = ClientStatus.BLOCKED;

        when(clientRepository.findByStatus(status)).thenReturn(List.of());

        List<ClientResponseDto> result = clientService.getClientsByStatus(status);

        assertThat(result).isEmpty();
    }

    @Test
    void getClientsWithActiveSubscriptions_ShouldReturnEmptyList_WhenNoClients() {
        when(clientRepository.findClientsWithActiveSubscriptions()).thenReturn(List.of());

        List<ClientResponseDto> result = clientService.getClientsWithActiveSubscriptions();

        assertThat(result).isEmpty();
    }

    @Test
    void getBookedClientsForSession_ShouldReturnEmptyList_WhenNoClients() {
        Long sessionId = 999L;

        when(clientRepository.findBookedClientsBySession(sessionId)).thenReturn(List.of());

        List<ClientResponseDto> result = clientService.getBookedClientsForSession(sessionId);

        assertThat(result).isEmpty();
    }

    @Test
    void createClientWithNewSubscription_ShouldThrowException_WhenSubscriptionNameExists() {
        ClientDto clientDto = new ClientDto();
        clientDto.setEmail("new@mail.com");
        clientDto.setFirstName("Иван");
        clientDto.setLastName("Иванов");

        SubscriptionDto subscriptionDto = new SubscriptionDto();
        subscriptionDto.setName("Существующий абонемент");
        subscriptionDto.setPrice(BigDecimal.valueOf(3000));
        subscriptionDto.setSubscriptionType(SubscriptionType.LIMITED);
        subscriptionDto.setMaxVisits(8);
        subscriptionDto.setDurationDays(30);

        Subscription existingSubscription = new Subscription();
        existingSubscription.setName("Существующий абонемент");

        when(clientRepository.existsByEmail(clientDto.getEmail())).thenReturn(false);
        when(subscriptionRepository.findByName(subscriptionDto.getName())).thenReturn(Optional.of(existingSubscription));

        assertThatThrownBy(() -> clientService.createClientWithNewSubscription(clientDto, subscriptionDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");

        verify(clientRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void removeSubscriptionFromClient_ShouldThrowException_WhenClientNotFound() {
        Long clientId = 999L;
        Long subscriptionId = 1L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.removeSubscriptionFromClient(clientId, subscriptionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClient_ShouldUpdateClient_WhenOnlyPhoneNumberChanged() {
        Long clientId = 1L;
        ClientPatchDto patchDto = new ClientPatchDto();
        patchDto.setPhoneNumber("+79998887766");

        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setEmail("ivan@mail.com");
        existingClient.setFirstName("Иван");
        existingClient.setLastName("Иванов");

        Client updatedClient = new Client();
        updatedClient.setId(clientId);
        updatedClient.setPhoneNumber("+79998887766");

        ClientResponseDto responseDto = new ClientResponseDto();
        responseDto.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);
        when(clientMapper.toResponseDto(updatedClient)).thenReturn(responseDto);

        ClientResponseDto result = clientService.updateClient(clientId, patchDto);

        assertThat(result).isNotNull();
        verify(clientMapper).updateEntity(patchDto, existingClient);
        verify(clientRepository).save(existingClient);
    }

}
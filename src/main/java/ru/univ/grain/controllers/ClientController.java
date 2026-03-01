package ru.univ.grain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.*;
import ru.univ.grain.entities.ClientStatus;
import ru.univ.grain.services.ClientService;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientResponseDto>> getAllClients() {
        final List<ClientResponseDto> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getClientById(@PathVariable final Long id) {
        final ClientResponseDto client = clientService.getClientResponseById(id);
        return client != null ? ResponseEntity.ok(client) : ResponseEntity.notFound().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClientResponseDto> getClientByEmail(@PathVariable final String email) {
        final ClientResponseDto client = clientService.getClientByEmail(email);
        return client != null ? ResponseEntity.ok(client) : ResponseEntity.notFound().build();
    }

    @GetMapping("/lastname/{lastName}")
    public ResponseEntity<List<ClientResponseDto>> getClientsByLastName(@PathVariable final String lastName) {
        final List<ClientResponseDto> clients = clientService.getClientsByLastName(lastName);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClientResponseDto>> getClientsByStatus(@PathVariable final ClientStatus status) {
        final List<ClientResponseDto> clients = clientService.getClientsByStatus(status);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/active-subscriptions")
    public ResponseEntity<List<ClientResponseDto>> getClientsWithActiveSubscriptions() {
        final List<ClientResponseDto> clients = clientService.getClientsWithActiveSubscriptions();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/booked-session/{sessionId}")
    public ResponseEntity<List<ClientResponseDto>> getBookedClientsForSession(@PathVariable final Long sessionId) {
        final List<ClientResponseDto> clients = clientService.getBookedClientsForSession(sessionId);
        return ResponseEntity.ok(clients);
    }

    @PostMapping
    public ResponseEntity<ClientResponseDto> createClient(@Valid @RequestBody final ClientDto dto) {
        final ClientResponseDto created = clientService.createClient(dto);
        return created != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(created)
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponseDto> patchClient(
            @PathVariable final Long id,
            @Valid @RequestBody final ClientPatchDto dto) {
        final ClientResponseDto updated = clientService.updateClient(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> updateClient(
            @PathVariable final Long id,
            @Valid @RequestBody final ClientPatchDto dto) {

        if (dto.getFirstName() == null || dto.getFirstName().isBlank() ||
                dto.getLastName() == null || dto.getLastName().isBlank() ||
                dto.getEmail() == null || dto.getEmail().isBlank() ||
                dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank() ||
                dto.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        final ClientResponseDto updated = clientService.updateClient(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable final Long id) {
        clientService.deleteClientById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clientId}/subscriptions/{subscriptionId}")
    public ResponseEntity<ClientResponseDto> addSubscriptionToClient(
            @PathVariable final Long clientId,
            @PathVariable final Long subscriptionId) {
        final ClientResponseDto updated = clientService.addSubscriptionToClient(clientId, subscriptionId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{clientId}/subscriptions/{subscriptionId}")
    public ResponseEntity<ClientResponseDto> removeSubscriptionFromClient(
            @PathVariable final Long clientId,
            @PathVariable final Long subscriptionId) {
        final ClientResponseDto updated = clientService.removeSubscriptionFromClient(clientId, subscriptionId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable final String email) {
        final boolean exists = clientService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/with-new-subscription")
    public ResponseEntity<ClientResponseDto> createClientWithNewSubscription(
            @Valid @RequestBody ClientWithSubscriptionRequest request) {

        final ClientResponseDto created = clientService.createClientWithNewSubscription(
                request.getClient(),
                request.getSubscription()
        );

        if (created == null) {
            if (clientService.existsByEmail(request.getClient().getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/with-new-subscription/sequential")
    public ResponseEntity<ClientResponseDto> createClientWithNewSubscriptionSequential(
            @Valid @RequestBody ClientWithSubscriptionRequest request) {

        final ClientResponseDto result = clientService.createClientWithNewSubscriptionSequential(
                request.getClient(),
                request.getSubscription()
        );

        if (result == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}

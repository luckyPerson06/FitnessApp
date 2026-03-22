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
    public ResponseEntity<ClientResponseDto> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientResponseById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClientResponseDto> getClientByEmail(@PathVariable final String email) {
        return  ResponseEntity.ok(clientService.getClientByEmail(email));
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
    public ResponseEntity<ClientResponseDto> createClient(@Valid @RequestBody ClientDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponseDto> patchClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientPatchDto dto) {
        return ResponseEntity.ok(clientService.updateClient(id, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientPatchDto dto) {
        return ResponseEntity.ok(clientService.updateClient(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable final Long id) {
        clientService.deleteClientById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clientId}/subscriptions/{subscriptionId}")
    public ResponseEntity<ClientResponseDto> addSubscriptionToClient(
            @PathVariable Long clientId,
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(clientService.addSubscriptionToClient(clientId, subscriptionId));
    }

    @DeleteMapping("/{clientId}/subscriptions/{subscriptionId}")
    public ResponseEntity<ClientResponseDto> removeSubscriptionFromClient(
            @PathVariable Long clientId,
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(clientService.removeSubscriptionFromClient(clientId, subscriptionId));
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

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


}

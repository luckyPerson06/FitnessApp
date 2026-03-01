package ru.univ.grain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;
import ru.univ.grain.services.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions() {
        final List<SubscriptionDto> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDto> getSubscriptionById(@PathVariable final Long id) {
        final SubscriptionDto subscription = subscriptionService.getSubscriptionById(id);
        return subscription != null ? ResponseEntity.ok(subscription) : ResponseEntity.notFound().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByType(@PathVariable final SubscriptionType type) {
        final List<SubscriptionDto> subscriptions = subscriptionService.getSubscriptionsByType(type);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByStatus(@PathVariable final SubscriptionStatus status) {
        final List<SubscriptionDto> subscriptions = subscriptionService.getSubscriptionsByStatus(status);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/workout-type/{workoutTypeId}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByWorkoutType(@PathVariable final Long workoutTypeId) {
        final List<SubscriptionDto> subscriptions = subscriptionService.getSubscriptionsByWorkoutType(workoutTypeId);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SubscriptionDto>> getActiveSubscriptions() {
        final List<SubscriptionDto> subscriptions = subscriptionService.getActiveSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<SubscriptionDto>> getExpiredSubscriptions() {
        final List<SubscriptionDto> subscriptions = subscriptionService.getExpiredSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/cancelled")
    public ResponseEntity<List<SubscriptionDto>> getCancelledSubscriptions() {
        final List<SubscriptionDto> subscriptions = subscriptionService.getCancelledSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/used")
    public ResponseEntity<List<SubscriptionDto>> getUsedSubscriptions() {
        final List<SubscriptionDto> subscriptions = subscriptionService.getUsedSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody final SubscriptionDto dto) {
        final SubscriptionDto created = subscriptionService.createSubscription(dto);
        return created != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(created)
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDto> updateSubscription(
            @PathVariable final Long id,
            @Valid @RequestBody final SubscriptionDto dto) {

        if (dto.getName() == null || dto.getName().isBlank() ||
                dto.getPrice() == null ||
                dto.getSubscriptionType() == null ||
                dto.getDurationDays() == null ||
                dto.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (dto.getSubscriptionType() == SubscriptionType.LIMITED &&
                (dto.getMaxVisits() == null || dto.getMaxVisits() <= 0)) {
            return ResponseEntity.badRequest().build();
        }

        if (dto.getSubscriptionType() == SubscriptionType.UNLIMITED &&
                dto.getMaxVisits() != null) {
            return ResponseEntity.badRequest().build();
        }

        final SubscriptionDto updated = subscriptionService.updateSubscription(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubscriptionDto> patchSubscription(
            @PathVariable final Long id,
            @Valid @RequestBody final SubscriptionDto dto) {

        if (dto.getSubscriptionType() != null && dto.getMaxVisits() != null &&
                dto.getSubscriptionType() == SubscriptionType.LIMITED &&
                dto.getMaxVisits() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        final SubscriptionDto updated = subscriptionService.updateSubscription(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable final Long id) {
        final boolean deleted = subscriptionService.deleteSubscription(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/expire")
    public ResponseEntity<SubscriptionDto> expireSubscription(@PathVariable final Long id) {
        final boolean expired = subscriptionService.expireSubscription(id);
        if (!expired) {
            return ResponseEntity.notFound().build();
        }
        final SubscriptionDto subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/{subscriptionId}/workout-types/{workoutTypeId}")
    public ResponseEntity<SubscriptionDto> addWorkoutType(
            @PathVariable final Long subscriptionId,
            @PathVariable final Long workoutTypeId) {
        final boolean added = subscriptionService.addWorkoutType(subscriptionId, workoutTypeId);
        if (!added) {
            return ResponseEntity.notFound().build();
        }
        final SubscriptionDto subscription = subscriptionService.getSubscriptionById(subscriptionId);
        return ResponseEntity.ok(subscription);
    }

    @DeleteMapping("/{subscriptionId}/workout-types/{workoutTypeId}")
    public ResponseEntity<SubscriptionDto> removeWorkoutType(
            @PathVariable final Long subscriptionId,
            @PathVariable final Long workoutTypeId) {
        final boolean removed = subscriptionService.removeWorkoutType(subscriptionId, workoutTypeId);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        final SubscriptionDto subscription = subscriptionService.getSubscriptionById(subscriptionId);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable final String name) {
        final boolean exists = subscriptionService.getSubscriptionByName(name) != null;
        return ResponseEntity.ok(exists);
    }
}

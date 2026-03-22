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
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDto> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByType(@PathVariable SubscriptionType type) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByType(type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByStatus(@PathVariable SubscriptionStatus status) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByStatus(status));
    }

    @GetMapping("/workout-type/{workoutTypeId}")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionsByWorkoutType(@PathVariable Long workoutTypeId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByWorkoutType(workoutTypeId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SubscriptionDto>> getActiveSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptions());
    }

    @GetMapping("/expired")
    public ResponseEntity<List<SubscriptionDto>> getExpiredSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getExpiredSubscriptions());
    }

    @GetMapping("/cancelled")
    public ResponseEntity<List<SubscriptionDto>> getCancelledSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getCancelledSubscriptions());
    }

    @GetMapping("/used")
    public ResponseEntity<List<SubscriptionDto>> getUsedSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getUsedSubscriptions());
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.createSubscription(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDto> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SubscriptionDto> patchSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/expire")
    public ResponseEntity<SubscriptionDto> expireSubscription(@PathVariable Long id) {
        subscriptionService.expireSubscription(id);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @PostMapping("/{subscriptionId}/workout-types/{workoutTypeId}")
    public ResponseEntity<SubscriptionDto> addWorkoutType(
            @PathVariable Long subscriptionId,
            @PathVariable Long workoutTypeId) {
        subscriptionService.addWorkoutType(subscriptionId, workoutTypeId);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(subscriptionId));
    }

    @DeleteMapping("/{subscriptionId}/workout-types/{workoutTypeId}")
    public ResponseEntity<SubscriptionDto> removeWorkoutType(
            @PathVariable Long subscriptionId,
            @PathVariable Long workoutTypeId) {
        subscriptionService.removeWorkoutType(subscriptionId, workoutTypeId);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(subscriptionId));
    }

    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionByName(name) != null);
    }
}

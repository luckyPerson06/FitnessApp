package ru.univ.grain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.entities.WorkoutCategory;
import ru.univ.grain.services.WorkoutTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/workout-types")
@RequiredArgsConstructor
public class WorkoutTypeController {

    private final WorkoutTypeService workoutTypeService;

    @GetMapping
    public ResponseEntity<List<WorkoutTypeDto>> getAllWorkoutTypes() {
        final List<WorkoutTypeDto> workoutTypes = workoutTypeService.getAllWorkoutTypes();
        return ResponseEntity.ok(workoutTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutTypeDto> getWorkoutTypeById(@PathVariable final Long id) {
        final WorkoutTypeDto workoutType = workoutTypeService.getWorkoutTypeById(id);
        return workoutType != null ? ResponseEntity.ok(workoutType) : ResponseEntity.notFound().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<WorkoutTypeDto> getWorkoutTypeByName(@PathVariable final String name) {
        final WorkoutTypeDto workoutType = workoutTypeService.getWorkoutTypeByName(name);
        return workoutType != null ? ResponseEntity.ok(workoutType) : ResponseEntity.notFound().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<WorkoutTypeDto>> getActiveWorkoutTypes() {
        final List<WorkoutTypeDto> workoutTypes = workoutTypeService.getActiveWorkoutTypes();
        return ResponseEntity.ok(workoutTypes);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<WorkoutTypeDto>> getWorkoutTypesByCategory(@PathVariable final WorkoutCategory category) {
        final List<WorkoutTypeDto> workoutTypes = workoutTypeService.getWorkoutTypesByCategory(category);
        return ResponseEntity.ok(workoutTypes);
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<WorkoutTypeDto>> getWorkoutTypesByTrainer(@PathVariable final Long trainerId) {
        final List<WorkoutTypeDto> workoutTypes = workoutTypeService.getWorkoutTypesByTrainer(trainerId);
        return ResponseEntity.ok(workoutTypes);
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<WorkoutTypeDto>> getWorkoutTypesBySubscription(@PathVariable final Long subscriptionId) {
        final List<WorkoutTypeDto> workoutTypes = workoutTypeService.getWorkoutTypesBySubscription(subscriptionId);
        return ResponseEntity.ok(workoutTypes);
    }

    @PostMapping
    public ResponseEntity<WorkoutTypeDto> createWorkoutType(@Valid @RequestBody final WorkoutTypeDto dto) {
        final WorkoutTypeDto created = workoutTypeService.createWorkoutType(dto);
        return created != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(created)
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutTypeDto> updateWorkoutType(
            @PathVariable final Long id,
            @Valid @RequestBody final WorkoutTypeDto dto) {

        if (dto.getName() == null || dto.getName().isBlank() ||
                dto.getCategory() == null ||
                dto.getIsActive() == null) {
            return ResponseEntity.badRequest().build();
        }

        final WorkoutTypeDto updated = workoutTypeService.updateWorkoutType(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkoutTypeDto> patchWorkoutType(
            @PathVariable final Long id,
            @Valid @RequestBody final WorkoutTypeDto dto) {

        final WorkoutTypeDto updated = workoutTypeService.updateWorkoutType(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<WorkoutTypeDto> deactivateWorkoutType(@PathVariable final Long id) {
        final boolean deactivated = workoutTypeService.deactivateWorkoutType(id);
        if (!deactivated) {
            return ResponseEntity.notFound().build();
        }
        final WorkoutTypeDto workoutType = workoutTypeService.getWorkoutTypeById(id);
        return ResponseEntity.ok(workoutType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkoutType(@PathVariable final Long id) {
        final boolean deleted = workoutTypeService.deleteWorkoutType(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable final String name) {
        final boolean exists = workoutTypeService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}

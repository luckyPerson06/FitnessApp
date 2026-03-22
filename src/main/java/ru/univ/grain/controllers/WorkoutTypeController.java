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
        return ResponseEntity.ok(workoutTypeService.getAllWorkoutTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutTypeDto> getWorkoutTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypeById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<WorkoutTypeDto> getWorkoutTypeByName(@PathVariable String name) {
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypeByName(name));
    }

    @GetMapping("/active")
    public ResponseEntity<List<WorkoutTypeDto>> getActiveWorkoutTypes() {
        return ResponseEntity.ok(workoutTypeService.getActiveWorkoutTypes());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<WorkoutTypeDto>> getWorkoutTypesByCategory(@PathVariable WorkoutCategory category) {
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypesByCategory(category));
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<WorkoutTypeDto>> getWorkoutTypesByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypesByTrainer(trainerId));
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<WorkoutTypeDto>> getWorkoutTypesBySubscription(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypesBySubscription(subscriptionId));
    }

    @PostMapping
    public ResponseEntity<WorkoutTypeDto> createWorkoutType(@Valid @RequestBody WorkoutTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutTypeService.createWorkoutType(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutTypeDto> updateWorkoutType(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutTypeDto dto) {
        return ResponseEntity.ok(workoutTypeService.updateWorkoutType(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkoutTypeDto> patchWorkoutType(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutTypeDto dto) {
        return ResponseEntity.ok(workoutTypeService.updateWorkoutType(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<WorkoutTypeDto> deactivateWorkoutType(@PathVariable Long id) {
        workoutTypeService.deactivateWorkoutType(id);
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypeById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkoutType(@PathVariable Long id) {
        workoutTypeService.deleteWorkoutType(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        return ResponseEntity.ok(workoutTypeService.existsByName(name));
    }
}

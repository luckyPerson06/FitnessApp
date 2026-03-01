package ru.univ.grain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.entities.TrainerStatus;
import ru.univ.grain.services.TrainerService;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @GetMapping
    public ResponseEntity<List<TrainerDto>> getAllTrainers() {
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainerDto> getTrainerById(@PathVariable final Long id) {
        final TrainerDto trainer = trainerService.getTrainerById(id);
        return trainer != null ? ResponseEntity.ok(trainer) : ResponseEntity.notFound().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TrainerDto>> getTrainersByStatus(@PathVariable final TrainerStatus status) {
        return ResponseEntity.ok(trainerService.getTrainersByStatus(status));
    }

    @GetMapping("/active")
    public ResponseEntity<List<TrainerDto>> getActiveTrainers() {
        return ResponseEntity.ok(trainerService.getActiveTrainers());
    }

    @GetMapping("/specialization/{name}")
    public ResponseEntity<List<TrainerDto>> getTrainersBySpecialization(@PathVariable final String name) {
        return ResponseEntity.ok(trainerService.getTrainersBySpecialization(name));
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<TrainerDto>> getTrainersWithSessionOnDay(@PathVariable final DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(trainerService.getTrainersWithSessionOnDay(dayOfWeek));
    }

    @PostMapping
    public ResponseEntity<TrainerDto> createTrainer(@Valid @RequestBody final TrainerDto dto) {
        final TrainerDto created = trainerService.createTrainer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainerDto> updateTrainer(
            @PathVariable final Long id,
            @Valid @RequestBody final TrainerDto dto) {

        if (dto.getFirstName() == null || dto.getFirstName().isBlank() ||
                dto.getLastName() == null || dto.getLastName().isBlank() ||
                dto.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        final TrainerDto updated = trainerService.updateTrainer(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TrainerDto> patchTrainer(
            @PathVariable final Long id,
            @Valid @RequestBody final TrainerDto dto) {

        final TrainerDto updated = trainerService.updateTrainer(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainer(@PathVariable final Long id) {
        return trainerService.deleteTrainer(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{trainerId}/specializations/{workoutTypeId}")
    public ResponseEntity<Void> addSpecialization(
            @PathVariable final Long trainerId,
            @PathVariable final Long workoutTypeId) {
        trainerService.addSpecialization(trainerId, workoutTypeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{trainerId}/specializations/{workoutTypeId}")
    public ResponseEntity<Boolean> removeSpecialization(
            @PathVariable final Long trainerId,
            @PathVariable final Long workoutTypeId) {
        final boolean removed = trainerService.removeSpecialization(trainerId, workoutTypeId);
        return ResponseEntity.ok(removed);
    }

    @GetMapping("/demo/nplus1")
    public ResponseEntity<Map<String, int[]>> demonstrateNPlus1() {
        final Map<String, int[]> result = new HashMap<>();
        result.put("N+1 проблема (много запросов)", trainerService.demonstrateNPlus1Problem());
        result.put("Решение (один запрос с JOIN)", trainerService.demonstrateSolution());
        return ResponseEntity.ok(result);
    }

}

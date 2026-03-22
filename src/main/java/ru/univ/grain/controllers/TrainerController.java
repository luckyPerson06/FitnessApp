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
    public ResponseEntity<TrainerDto> getTrainerById(@PathVariable Long id) {
        return ResponseEntity.ok(trainerService.getTrainerById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TrainerDto>> getTrainersByStatus(@PathVariable TrainerStatus status) {
        return ResponseEntity.ok(trainerService.getTrainersByStatus(status));
    }

    @GetMapping("/active")
    public ResponseEntity<List<TrainerDto>> getActiveTrainers() {
        return ResponseEntity.ok(trainerService.getActiveTrainers());
    }

    @GetMapping("/specialization/{name}")
    public ResponseEntity<List<TrainerDto>> getTrainersBySpecialization(@PathVariable String name) {
        return ResponseEntity.ok(trainerService.getTrainersBySpecialization(name));
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<TrainerDto>> getTrainersWithSessionOnDay(@PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(trainerService.getTrainersWithSessionOnDay(dayOfWeek));
    }

    @PostMapping
    public ResponseEntity<TrainerDto> createTrainer(@Valid @RequestBody TrainerDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.createTrainer(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainerDto> updateTrainer(
            @PathVariable Long id,
            @Valid @RequestBody TrainerDto dto) {
        return ResponseEntity.ok(trainerService.updateTrainer(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TrainerDto> patchTrainer(
            @PathVariable Long id,
            @Valid @RequestBody TrainerDto dto) {
        return ResponseEntity.ok(trainerService.updateTrainer(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainer(@PathVariable Long id) {
        trainerService.deleteTrainer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{trainerId}/specializations/{workoutTypeId}")
    public ResponseEntity<Void> addSpecialization(
            @PathVariable Long trainerId,
            @PathVariable Long workoutTypeId) {
        trainerService.addSpecialization(trainerId, workoutTypeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{trainerId}/specializations/{workoutTypeId}")
    public ResponseEntity<Void> removeSpecialization(
            @PathVariable Long trainerId,
            @PathVariable Long workoutTypeId) {
        trainerService.removeSpecialization(trainerId, workoutTypeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/demo/nplus1")
    public ResponseEntity<Map<String, int[]>> demonstrateNPlus1() {
        final Map<String, int[]> result = new HashMap<>();
        result.put("N+1 проблема (много запросов)", trainerService.demonstrateNPlus1Problem());
        result.put("Решение (один запрос с JOIN)", trainerService.demonstrateSolution());
        return ResponseEntity.ok(result);
    }
}

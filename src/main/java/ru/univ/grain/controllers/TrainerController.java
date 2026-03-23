package ru.univ.grain.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.entities.TrainerStatus;
import ru.univ.grain.services.TrainerService;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Тренеры", description = "Управление тренерами")
@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @Operation(summary = "Получить всех тренеров")
    @GetMapping
    public ResponseEntity<List<TrainerDto>> getAllTrainers() {
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @Operation(summary = "Получить тренера по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тренер найден"),
            @ApiResponse(responseCode = "404", description = "Тренер не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TrainerDto> getTrainerById(@Parameter(description = "ID тренера", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(trainerService.getTrainerById(id));
    }

    @Operation(summary = "Получить тренеров по статусу")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TrainerDto>> getTrainersByStatus(@Parameter(description = "Статус тренера", example = "ACTIVE") @PathVariable TrainerStatus status) {
        return ResponseEntity.ok(trainerService.getTrainersByStatus(status));
    }

    @Operation(summary = "Получить активных тренеров")
    @GetMapping("/active")
    public ResponseEntity<List<TrainerDto>> getActiveTrainers() {
        return ResponseEntity.ok(trainerService.getActiveTrainers());
    }

    @Operation(summary = "Найти тренеров по специализации")
    @GetMapping("/specialization/{name}")
    public ResponseEntity<List<TrainerDto>> getTrainersBySpecialization(@Parameter(description = "Название специализации", example = "Йога") @PathVariable String name) {
        return ResponseEntity.ok(trainerService.getTrainersBySpecialization(name));
    }

    @Operation(summary = "Найти тренеров, ведущих тренировки в указанный день")
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<TrainerDto>> getTrainersWithSessionOnDay(@Parameter(description = "День недели", example = "MONDAY") @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(trainerService.getTrainersWithSessionOnDay(dayOfWeek));
    }

    @Operation(summary = "Создать нового тренера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Тренер создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<TrainerDto> createTrainer(@Valid @RequestBody TrainerDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.createTrainer(dto));
    }

    @Operation(summary = "Полное обновление тренера")
    @PutMapping("/{id}")
    public ResponseEntity<TrainerDto> updateTrainer(
            @Parameter(description = "ID тренера", example = "1") @PathVariable Long id,
            @Valid @RequestBody TrainerDto dto) {
        return ResponseEntity.ok(trainerService.updateTrainer(id, dto));
    }

    @Operation(summary = "Частичное обновление тренера")
    @PatchMapping("/{id}")
    public ResponseEntity<TrainerDto> patchTrainer(
            @Parameter(description = "ID тренера", example = "1") @PathVariable Long id,
            @Valid @RequestBody TrainerDto dto) {
        return ResponseEntity.ok(trainerService.updateTrainer(id, dto));
    }

    @Operation(summary = "Удалить тренера")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Тренер удалён"),
            @ApiResponse(responseCode = "404", description = "Тренер не найден", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainer(@Parameter(description = "ID тренера", example = "1") @PathVariable Long id) {
        trainerService.deleteTrainer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить специализацию тренеру")
    @PostMapping("/{trainerId}/specializations/{workoutTypeId}")
    public ResponseEntity<Void> addSpecialization(
            @Parameter(description = "ID тренера", example = "1") @PathVariable Long trainerId,
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable Long workoutTypeId) {
        trainerService.addSpecialization(trainerId, workoutTypeId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить специализацию у тренера")
    @DeleteMapping("/{trainerId}/specializations/{workoutTypeId}")
    public ResponseEntity<Void> removeSpecialization(
            @Parameter(description = "ID тренера", example = "1") @PathVariable Long trainerId,
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable Long workoutTypeId) {
        trainerService.removeSpecialization(trainerId, workoutTypeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Демонстрация проблемы N+1 и её решения")
    @GetMapping("/demo/nplus1")
    public ResponseEntity<Map<String, int[]>> demonstrateNPlus1() {
        final Map<String, int[]> result = new HashMap<>();
        result.put("N+1 проблема (много запросов)", trainerService.demonstrateNPlus1Problem());
        result.put("Решение (один запрос с JOIN)", trainerService.demonstrateSolution());
        return ResponseEntity.ok(result);
    }
}

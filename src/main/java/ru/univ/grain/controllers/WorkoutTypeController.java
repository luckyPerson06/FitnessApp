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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.services.WorkoutTypeService;

import java.util.List;

@Tag(name = "Типы тренировок", description = "Управление типами тренировок")
@RestController
    @RequestMapping("/api/workout-types")
@RequiredArgsConstructor
public class WorkoutTypeController {

    private final WorkoutTypeService workoutTypeService;

    @Operation(summary = "Получить все типы тренировок")
    @GetMapping
    public ResponseEntity<List<WorkoutTypeDto>> getAllWorkoutTypes() {
        return ResponseEntity.ok(workoutTypeService.getAllWorkoutTypes());
    }

    @Operation(summary = "Получить тип тренировки по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тип тренировки найден"),
            @ApiResponse(responseCode = "404", description = "Тип тренировки не найден",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutTypeDto> getWorkoutTypeById(
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable final Long id) {
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypeById(id));
    }

    @Operation(summary = "Получить тип тренировки по названию")
    @GetMapping("/name/{name}")
    public ResponseEntity<WorkoutTypeDto> getWorkoutTypeByName(
            @Parameter(description = "Название типа тренировки", example = "Йога") @PathVariable final String name) {
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypeByName(name));
    }

    @Operation(summary = "Получить активные типы тренировок")
    @GetMapping("/active")
    public ResponseEntity<List<WorkoutTypeDto>> getActiveWorkoutTypes() {
        return ResponseEntity.ok(workoutTypeService.getActiveWorkoutTypes());
    }

    @Operation(summary = "Получить типы тренировок тренера")
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<WorkoutTypeDto>> getWorkoutTypesByTrainer(
            @Parameter(description = "ID тренера", example = "1") @PathVariable final Long trainerId) {
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypesByTrainer(trainerId));
    }

    @Operation(summary = "Создать новый тип тренировки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Тип тренировки создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "409", description = "Тип тренировки с таким названием уже существует")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutTypeDto> createWorkoutType(@Valid @RequestBody final WorkoutTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutTypeService.createWorkoutType(dto));
    }

    @Operation(summary = "Обновить тип тренировки")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutTypeDto> updateWorkoutType(
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable final Long id,
            @Valid @RequestBody final WorkoutTypeDto dto) {
        return ResponseEntity.ok(workoutTypeService.updateWorkoutType(id, dto));
    }

    @Operation(summary = "Частичное обновление типа тренировки")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutTypeDto> patchWorkoutType(
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable final Long id,
            @Valid @RequestBody final WorkoutTypeDto dto) {
        return ResponseEntity.ok(workoutTypeService.updateWorkoutType(id, dto));
    }

    @Operation(summary = "Деактивировать тип тренировки")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutTypeDto> deactivateWorkoutType(
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable final Long id) {
        workoutTypeService.deactivateWorkoutType(id);
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypeById(id));
    }

    @Operation(summary = "Удалить тип тренировки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Тип тренировки удалён"),
            @ApiResponse(responseCode = "404", description = "Тип тренировки не найден"),
            @ApiResponse(responseCode = "400", description = "Невозможно удалить: есть связанные данные")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkoutType(
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable final Long id) {
        workoutTypeService.deleteWorkoutType(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Проверить существование типа тренировки по названию")
    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(
            @Parameter(description = "Название типа тренировки", example = "Йога") @PathVariable final String name) {
        return ResponseEntity.ok(workoutTypeService.existsByName(name));
    }

    @Operation(summary = "Получить тренеров по типу тренировки")
    @GetMapping("/{id}/trainers")
    public ResponseEntity<List<TrainerDto>> getTrainersByWorkoutType(@PathVariable final Long id) {
        return ResponseEntity.ok(workoutTypeService.getTrainersByWorkoutType(id));
    }

}

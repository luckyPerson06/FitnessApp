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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.entities.WorkoutSessionStatus;
import ru.univ.grain.services.WorkoutSessionService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "Тренировки", description = "Управление тренировочными сессиями")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @Operation(summary = "Получить все тренировки")
    @GetMapping
    public ResponseEntity<List<WorkoutSessionDto>> getAllSessions() {
        return ResponseEntity.ok(workoutSessionService.getAllSessions());
    }

    @Operation(summary = "Получить тренировку по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тренировка найдена"),
            @ApiResponse(responseCode = "404", description = "Тренировка не найдена",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSessionDto> getSessionById(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable final Long id) {
        return ResponseEntity.ok(workoutSessionService.getSessionById(id));
    }

    @Operation(summary = "Получить тренировки тренера")
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByTrainer(
            @Parameter(description = "ID тренера", example = "1") @PathVariable final Long trainerId) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainer(trainerId));
    }

    @Operation(summary = "Получить тренировки по типу")
    @GetMapping("/workout-type/{workoutTypeId}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByWorkoutType(
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable final Long workoutTypeId) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByWorkoutType(workoutTypeId));
    }

    @Operation(summary = "Получить тренировки по дню недели")
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByDay(
            @Parameter(description = "День недели", example = "MONDAY") @PathVariable final DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByDay(dayOfWeek));
    }

    @Operation(summary = "Получить активные тренировки по дню недели")
    @GetMapping("/day/{dayOfWeek}/active")
    public ResponseEntity<List<WorkoutSessionDto>> getActiveSessionsByDay(
            @Parameter(description = "День недели", example = "MONDAY") @PathVariable final DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(workoutSessionService.getActiveSessionsByDay(dayOfWeek));
    }

    @Operation(summary = "Получить тренировки по дате (для календаря)")
    @GetMapping("/date/{date}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsForDate(
            @Parameter(description = "Дата", example = "2026-04-14")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date) {
        return ResponseEntity.ok(workoutSessionService.getSessionsForDate(date));
    }

    @Operation(summary = "Получить тренировки в диапазоне дат")
    @GetMapping("/date-range")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByDateRange(
            @Parameter(description = "Дата начала", example = "2026-04-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @Parameter(description = "Дата окончания", example = "2026-04-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByDateRange(from, to));
    }

    @Operation(summary = "Получить сегодняшние тренировки")
    @GetMapping("/today")
    public ResponseEntity<List<WorkoutSessionDto>> getTodaySessions() {
        return ResponseEntity.ok(workoutSessionService.getTodaySessions());
    }

    @Operation(summary = "Получить тренировки по времени")
    @GetMapping("/time")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByTime(
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam final DayOfWeek dayOfWeek,
            @Parameter(description = "Время", example = "10:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime time) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTime(dayOfWeek, time));
    }

    @Operation(summary = "Получить количество записанных на тренировку")
    @GetMapping("/{sessionId}/booked-count")
    public ResponseEntity<Long> getBookedCount(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable final Long sessionId) {
        return ResponseEntity.ok(workoutSessionService.getBookedCount(sessionId));
    }

    @Operation(summary = "Проверить наличие свободных мест")
    @GetMapping("/{sessionId}/available-spots")
    public ResponseEntity<Boolean> hasAvailableSpots(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable final Long sessionId) {
        return ResponseEntity.ok(workoutSessionService.hasAvailableSpots(sessionId));
    }

    @Operation(summary = "Получить тренировки по статусу")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByStatus(
            @Parameter(description = "Статус тренировки", example = "SCHEDULED") @PathVariable final WorkoutSessionStatus status) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByStatus(status));
    }

    @Operation(summary = "Проверить доступность тренера")
    @GetMapping("/check-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> isTrainerAvailable(
            @Parameter(description = "ID тренера", example = "1") @RequestParam final Long trainerId,
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam final DayOfWeek dayOfWeek,
            @Parameter(description = "Время начала", example = "10:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime start,
            @Parameter(description = "Время окончания", example = "11:30:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime end) {
        return ResponseEntity.ok(workoutSessionService.isTrainerAvailable(trainerId, dayOfWeek, start, end));
    }

    @Operation(summary = "Найти пересекающиеся тренировки")
    @GetMapping("/overlapping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutSessionDto>> findOverlappingSessions(
            @Parameter(description = "ID тренера", example = "1") @RequestParam final Long trainerId,
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam final DayOfWeek dayOfWeek,
            @Parameter(description = "Время начала", example = "10:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime start,
            @Parameter(description = "Время окончания", example = "11:30:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime end) {
        return ResponseEntity.ok(workoutSessionService.findOverlappingSessions(trainerId, dayOfWeek, start, end));
    }

    @Operation(summary = "Создать новую тренировку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Тренировка создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или пересечение расписания")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutSessionDto> createSession(@Valid @RequestBody final WorkoutSessionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.createSession(dto));
    }

    @Operation(summary = "Обновить тренировку")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutSessionDto> updateSession(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable final Long id,
            @Valid @RequestBody final WorkoutSessionDto dto) {
        return ResponseEntity.ok(workoutSessionService.updateSession(id, dto));
    }

    @Operation(summary = "Частичное обновление тренировки")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutSessionDto> patchSession(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable final Long id,
            @Valid @RequestBody final WorkoutSessionDto dto) {
        return ResponseEntity.ok(workoutSessionService.updateSession(id, dto));
    }

    @Operation(summary = "Обновить статус тренировки")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutSessionDto> updateSessionStatus(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable final Long id,
            @Parameter(description = "Новый статус", example = "CANCELLED") @RequestParam final WorkoutSessionStatus status) {
        return ResponseEntity.ok(workoutSessionService.updateSessionStatus(id, status));
    }

    @Operation(summary = "Удалить тренировку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Тренировка удалена"),
            @ApiResponse(responseCode = "404", description = "Тренировка не найдена"),
            @ApiResponse(responseCode = "400", description = "Невозможно удалить: есть будущие записи")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable final Long id) {
        workoutSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить ближайшие тренировки по типу")
    @GetMapping("/workout-type/{workoutTypeId}/upcoming")
    public ResponseEntity<List<WorkoutSessionDto>> getUpcomingByWorkoutType(
            @Parameter(description = "ID типа тренировки", example = "1") @PathVariable final Long workoutTypeId) {
        return ResponseEntity.ok(workoutSessionService.getUpcomingByWorkoutType(workoutTypeId));
    }
}

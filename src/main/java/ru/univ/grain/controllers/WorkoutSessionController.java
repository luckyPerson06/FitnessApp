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
import org.springframework.data.domain.Page;
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
            @ApiResponse(responseCode = "404", description = "Тренировка не найдена", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSessionDto> getSessionById(@Parameter(description = "ID тренировки", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(workoutSessionService.getSessionById(id));
    }

    @Operation(summary = "Получить тренировки тренера")
    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByTrainer(@Parameter(description = "ID тренера", example = "1") @PathVariable Long trainerId) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainer(trainerId));
    }

    @Operation(summary = "Получить тренировки по типу")
    @GetMapping("/workout-type/{workoutTypeId}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByWorkoutType(@Parameter(description = "ID типа тренировки", example = "1") @PathVariable Long workoutTypeId) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByWorkoutType(workoutTypeId));
    }

    @Operation(summary = "Получить тренировки по дню недели")
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByDay(@Parameter(description = "День недели", example = "MONDAY") @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByDay(dayOfWeek));
    }

    @Operation(summary = "Получить активные тренировки по дню недели")
    @GetMapping("/day/{dayOfWeek}/active")
    public ResponseEntity<List<WorkoutSessionDto>> getActiveSessionsByDay(@Parameter(description = "День недели", example = "MONDAY") @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(workoutSessionService.getActiveSessionsByDay(dayOfWeek));
    }

    @Operation(summary = "Получить сегодняшние тренировки")
    @GetMapping("/today")
    public ResponseEntity<List<WorkoutSessionDto>> getTodaySessions() {
        return ResponseEntity.ok(workoutSessionService.getTodaySessions());
    }

    @Operation(summary = "Получить тренировки по времени")
    @GetMapping("/time")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByTime(
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam DayOfWeek dayOfWeek,
            @Parameter(description = "Время", example = "10:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTime(dayOfWeek, time));
    }

    @Operation(summary = "Получить количество записанных на тренировку")
    @GetMapping("/{sessionId}/booked-count")
    public ResponseEntity<Long> getBookedCount(@Parameter(description = "ID тренировки", example = "1") @PathVariable Long sessionId) {
        return ResponseEntity.ok(workoutSessionService.getBookedCount(sessionId));
    }

    @Operation(summary = "Проверить наличие свободных мест")
    @GetMapping("/{sessionId}/available-spots")
    public ResponseEntity<Boolean> hasAvailableSpots(@Parameter(description = "ID тренировки", example = "1") @PathVariable Long sessionId) {
        return ResponseEntity.ok(workoutSessionService.hasAvailableSpots(sessionId));
    }

    @Operation(summary = "Поиск тренировок по фамилии тренера и дню (JPQL + пагинация)")
    @GetMapping("/by-trainer-name-and-day")
    public ResponseEntity<Page<WorkoutSessionDto>> getSessionsByTrainerNameAndDay(
            @Parameter(description = "Фамилия тренера", example = "Смирнова") @RequestParam String trainerLastName,
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam DayOfWeek dayOfWeek,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainerLastNameAndDay(
                trainerLastName, dayOfWeek, page, size));
    }

    @Operation(summary = "Поиск тренировок по фамилии тренера и дню (с кэшем)")
    @GetMapping("/by-trainer-name-and-day/cached")
    public ResponseEntity<Page<WorkoutSessionDto>> getSessionsByTrainerNameAndDayCached(
            @Parameter(description = "Фамилия тренера", example = "Смирнова") @RequestParam String trainerLastName,
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam DayOfWeek dayOfWeek,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainerLastNameAndDayCached(
                trainerLastName, dayOfWeek, page, size));
    }

    @Operation(summary = "Поиск тренировок по фамилии тренера и дню (Native SQL + пагинация)")
    @GetMapping("/by-trainer-name-and-day-native")
    public ResponseEntity<Page<WorkoutSessionDto>> getSessionsByTrainerNameAndDayNative(
            @Parameter(description = "Фамилия тренера", example = "Смирнова") @RequestParam String trainerLastName,
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam DayOfWeek dayOfWeek,
            @Parameter(description = "Номер страницы", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "10") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainerLastNameAndDayNative(
                trainerLastName, dayOfWeek, page, size));
    }

    @Operation(summary = "Получить тренировки по статусу")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByStatus(@Parameter(description = "Статус тренировки", example = "SCHEDULED") @PathVariable WorkoutSessionStatus status) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByStatus(status));
    }

    @Operation(summary = "Получить все запланированные тренировки")
    @GetMapping("/scheduled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutSessionDto>> getAllScheduledSessions() {
        return ResponseEntity.ok(workoutSessionService.getAllScheduledSessions());
    }

    @Operation(summary = "Проверить доступность тренера")
    @GetMapping("/check-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> isTrainerAvailable(
            @Parameter(description = "ID тренера", example = "1") @RequestParam Long trainerId,
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam DayOfWeek dayOfWeek,
            @Parameter(description = "Время начала", example = "10:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
            @Parameter(description = "Время окончания", example = "11:30:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end) {
        return ResponseEntity.ok(workoutSessionService.isTrainerAvailable(trainerId, dayOfWeek, start, end));
    }

    @Operation(summary = "Найти пересекающиеся тренировки")
    @GetMapping("/overlapping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutSessionDto>> findOverlappingSessions(
            @Parameter(description = "ID тренера", example = "1") @RequestParam Long trainerId,
            @Parameter(description = "День недели", example = "MONDAY") @RequestParam DayOfWeek dayOfWeek,
            @Parameter(description = "Время начала", example = "10:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
            @Parameter(description = "Время окончания", example = "11:30:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end) {
        return ResponseEntity.ok(workoutSessionService.findOverlappingSessions(trainerId, dayOfWeek, start, end));
    }

    @Operation(summary = "Создать новую тренировку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Тренировка создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или пересечение расписания", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Тренер или тип тренировки не найдены", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutSessionDto> createSession(@Valid @RequestBody WorkoutSessionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.createSession(dto));
    }

    @Operation(summary = "Обновить тренировку")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutSessionDto> updateSession(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable Long id,
            @Valid @RequestBody WorkoutSessionDto dto) {
        return ResponseEntity.ok(workoutSessionService.updateSession(id, dto));
    }

    @Operation(summary = "Частичное обновление тренировки")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutSessionDto> patchSession(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable Long id,
            @Valid @RequestBody WorkoutSessionDto dto) {
        return ResponseEntity.ok(workoutSessionService.updateSession(id, dto));
    }

    @Operation(summary = "Обновить статус тренировки")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkoutSessionDto> updateSessionStatus(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable Long id,
            @Parameter(description = "Новый статус", example = "CANCELLED") @RequestParam WorkoutSessionStatus status) {
        return ResponseEntity.ok(workoutSessionService.updateSessionStatus(id, status));
    }

    @Operation(summary = "Удалить тренировку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Тренировка удалена"),
            @ApiResponse(responseCode = "404", description = "Тренировка не найдена", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "400", description = "Невозможно удалить: есть будущие записи", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(@Parameter(description = "ID тренировки", example = "1") @PathVariable Long id) {
        workoutSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Массовое создание тренировок (с транзакцией)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Все тренировки созданы"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или бизнес-правил", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/bulk/with-transaction")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutSessionDto>> createSessionsBulkWithTransaction(
            @Valid @RequestBody WorkoutSessionBulkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.createSessionsBulkWithTransaction(request.getSessions()));
    }

    @Operation(summary = "Массовое создание тренировок (без транзакции)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/bulk/without-transaction")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutSessionDto>> createSessionsBulkWithoutTransaction(
            @Valid @RequestBody WorkoutSessionBulkRequest request) {
        final List<WorkoutSessionDto> result = workoutSessionService.createSessionsBulkWithoutTransaction(request.getSessions());
        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
    }
}

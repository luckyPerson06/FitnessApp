package ru.univ.grain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.entities.WorkoutSessionStatus;
import ru.univ.grain.services.WorkoutSessionService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @GetMapping
    public ResponseEntity<List<WorkoutSessionDto>> getAllSessions() {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSessionDto> getSessionById(@PathVariable final Long id) {
        final WorkoutSessionDto session = workoutSessionService.getSessionById(id);
        return session != null ? ResponseEntity.ok(session) : ResponseEntity.notFound().build();
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByTrainer(@PathVariable final Long trainerId) {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getSessionsByTrainer(trainerId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/workout-type/{workoutTypeId}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByWorkoutType(@PathVariable final Long workoutTypeId) {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getSessionsByWorkoutType(workoutTypeId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByDay(@PathVariable final DayOfWeek dayOfWeek) {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getSessionsByDay(dayOfWeek);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/day/{dayOfWeek}/active")
    public ResponseEntity<List<WorkoutSessionDto>> getActiveSessionsByDay(@PathVariable final DayOfWeek dayOfWeek) {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getActiveSessionsByDay(dayOfWeek);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/today")
    public ResponseEntity<List<WorkoutSessionDto>> getTodaySessions() {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getTodaySessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByStatus(@PathVariable final WorkoutSessionStatus status) {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getSessionsByStatus(status);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<WorkoutSessionDto>> getAllScheduledSessions() {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getAllScheduledSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/time")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByTime(
            @RequestParam final DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime time) {
        final List<WorkoutSessionDto> sessions = workoutSessionService.getSessionsByTime(dayOfWeek, time);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> isTrainerAvailable(
            @RequestParam final Long trainerId,
            @RequestParam final DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime end) {
        final boolean available = workoutSessionService.isTrainerAvailable(trainerId, dayOfWeek, start, end);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/overlapping")
    public ResponseEntity<List<WorkoutSessionDto>> findOverlappingSessions(
            @RequestParam final Long trainerId,
            @RequestParam final DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) final LocalTime end) {
        final List<WorkoutSessionDto> sessions = workoutSessionService.findOverlappingSessions(trainerId, dayOfWeek, start, end);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}/booked-count")
    public ResponseEntity<Long> getBookedCount(@PathVariable final Long sessionId) {
        final long count = workoutSessionService.getBookedCount(sessionId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{sessionId}/available-spots")
    public ResponseEntity<Boolean> hasAvailableSpots(@PathVariable final Long sessionId) {
        final boolean available = workoutSessionService.hasAvailableSpots(sessionId);
        return ResponseEntity.ok(available);
    }

    @PostMapping
    public ResponseEntity<WorkoutSessionDto> createSession(@Valid @RequestBody final WorkoutSessionDto dto) {
        final WorkoutSessionDto created = workoutSessionService.createSession(dto);
        return created != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(created)
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutSessionDto> updateSession(
            @PathVariable final Long id,
            @Valid @RequestBody final WorkoutSessionDto dto) {

        // Для PUT все поля обязательны
        if (dto.getTrainerId() == null ||
                dto.getWorkoutTypeId() == null ||
                dto.getDayOfWeek() == null ||
                dto.getStartTime() == null ||
                dto.getEndTime() == null ||
                dto.getMaxParticipants() == null ||
                dto.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            return ResponseEntity.badRequest().build();
        }

        final WorkoutSessionDto updated = workoutSessionService.updateSession(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkoutSessionDto> patchSession(
            @PathVariable final Long id,
            @Valid @RequestBody final WorkoutSessionDto dto) {

        if (dto.getStartTime() != null && dto.getEndTime() != null &&
                dto.getStartTime().isAfter(dto.getEndTime())) {
            return ResponseEntity.badRequest().build();
        }

        final WorkoutSessionDto updated = workoutSessionService.updateSession(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WorkoutSessionDto> updateSessionStatus(
            @PathVariable final Long id,
            @RequestParam final WorkoutSessionStatus status) {
        final WorkoutSessionDto updated = workoutSessionService.updateSessionStatus(id, status);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable final Long id) {
        final boolean deleted = workoutSessionService.deleteSession(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

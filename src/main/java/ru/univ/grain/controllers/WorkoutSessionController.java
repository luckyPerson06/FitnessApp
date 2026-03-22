package ru.univ.grain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
        return ResponseEntity.ok(workoutSessionService.getAllSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSessionDto> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutSessionService.getSessionById(id));
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainer(trainerId));
    }

    @GetMapping("/workout-type/{workoutTypeId}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByWorkoutType(@PathVariable Long workoutTypeId) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByWorkoutType(workoutTypeId));
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByDay(@PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByDay(dayOfWeek));
    }

    @GetMapping("/day/{dayOfWeek}/active")
    public ResponseEntity<List<WorkoutSessionDto>> getActiveSessionsByDay(@PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(workoutSessionService.getActiveSessionsByDay(dayOfWeek));
    }

    @GetMapping("/today")
    public ResponseEntity<List<WorkoutSessionDto>> getTodaySessions() {
        return ResponseEntity.ok(workoutSessionService.getTodaySessions());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByStatus(@PathVariable WorkoutSessionStatus status) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByStatus(status));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<WorkoutSessionDto>> getAllScheduledSessions() {
        return ResponseEntity.ok(workoutSessionService.getAllScheduledSessions());
    }

    @GetMapping("/time")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsByTime(
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTime(dayOfWeek, time));
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> isTrainerAvailable(
            @RequestParam Long trainerId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end) {
        return ResponseEntity.ok(workoutSessionService.isTrainerAvailable(trainerId, dayOfWeek, start, end));
    }

    @GetMapping("/overlapping")
    public ResponseEntity<List<WorkoutSessionDto>> findOverlappingSessions(
            @RequestParam Long trainerId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end) {
        return ResponseEntity.ok(workoutSessionService.findOverlappingSessions(trainerId, dayOfWeek, start, end));
    }

    @GetMapping("/{sessionId}/booked-count")
    public ResponseEntity<Long> getBookedCount(@PathVariable Long sessionId) {
        return ResponseEntity.ok(workoutSessionService.getBookedCount(sessionId));
    }

    @GetMapping("/{sessionId}/available-spots")
    public ResponseEntity<Boolean> hasAvailableSpots(@PathVariable Long sessionId) {
        return ResponseEntity.ok(workoutSessionService.hasAvailableSpots(sessionId));
    }

    @PostMapping
    public ResponseEntity<WorkoutSessionDto> createSession(@Valid @RequestBody WorkoutSessionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutSessionService.createSession(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutSessionDto> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutSessionDto dto) {
        return ResponseEntity.ok(workoutSessionService.updateSession(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkoutSessionDto> patchSession(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutSessionDto dto) {
        return ResponseEntity.ok(workoutSessionService.updateSession(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WorkoutSessionDto> updateSessionStatus(
            @PathVariable Long id,
            @RequestParam WorkoutSessionStatus status) {
        return ResponseEntity.ok(workoutSessionService.updateSessionStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        workoutSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-trainer-name-and-day")
    public ResponseEntity<Page<WorkoutSessionDto>> getSessionsByTrainerNameAndDay(
            @RequestParam String trainerLastName,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainerLastNameAndDay(
                trainerLastName, dayOfWeek, page, size));
    }

    @GetMapping("/by-trainer-name-and-day/cached")
    public ResponseEntity<Page<WorkoutSessionDto>> getSessionsByTrainerNameAndDayCached(
            @RequestParam String trainerLastName,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainerLastNameAndDayCached(
                trainerLastName, dayOfWeek, page, size));
    }

    @GetMapping("/by-trainer-name-and-day-native")
    public ResponseEntity<Page<WorkoutSessionDto>> getSessionsByTrainerNameAndDayNative(
            @RequestParam String trainerLastName,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(workoutSessionService.getSessionsByTrainerLastNameAndDayNative(
                trainerLastName, dayOfWeek, page, size));
    }
}

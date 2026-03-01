package ru.univ.grain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.services.VisitService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @GetMapping
    public ResponseEntity<List<VisitDto>> getAllVisits() {
        final List<VisitDto> visits = visitService.getAllVisits();
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitDto> getVisitById(@PathVariable final Long id) {
        final VisitDto visit = visitService.getVisitById(id);
        return visit != null ? ResponseEntity.ok(visit) : ResponseEntity.notFound().build();
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<VisitDto>> getClientVisits(@PathVariable final Long clientId) {
        final List<VisitDto> visits = visitService.getClientVisits(clientId);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/client/{clientId}/upcoming")
    public ResponseEntity<List<VisitDto>> getClientUpcomingVisits(@PathVariable final Long clientId) {
        final List<VisitDto> visits = visitService.getClientUpcomingVisits(clientId);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/client/{clientId}/history")
    public ResponseEntity<List<VisitDto>> getClientHistory(
            @PathVariable final Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        final List<VisitDto> visits = visitService.getClientHistory(clientId, from, to);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/client/{clientId}/count")
    public ResponseEntity<Long> getClientVisitsCount(
            @PathVariable final Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        final long count = visitService.getClientVisitsCount(clientId, from, to);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<VisitDto>> getScheduleVisits(@PathVariable final Long sessionId) {
        final List<VisitDto> visits = visitService.getScheduleVisits(sessionId);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/today")
    public ResponseEntity<List<VisitDto>> getTodayVisits() {
        final List<VisitDto> visits = visitService.getTodayVisits();
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/subscription/{subscriptionId}/used")
    public ResponseEntity<Long> getSubscriptionUsedVisits(@PathVariable final Long subscriptionId) {
        final long count = visitService.getSubscriptionUsedVisits(subscriptionId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/hourly")
    public ResponseEntity<List<Object[]>> getVisitsByHourStats() {
        final List<Object[]> stats = visitService.getVisitsByHourStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    public ResponseEntity<VisitDto> createVisit(@Valid @RequestBody final VisitDto dto) {
        final VisitDto created = visitService.createVisit(dto);
        return created != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(created)
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/book")
    public ResponseEntity<VisitDto> bookWorkout(
            @RequestParam final Long clientId,
            @RequestParam final Long sessionId,
            @RequestParam final Long subscriptionId) {
        final VisitDto booked = visitService.bookWorkout(clientId, sessionId, subscriptionId);
        return booked != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(booked)
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitDto> updateVisit(
            @PathVariable final Long id,
            @Valid @RequestBody final VisitDto dto) {

        if (dto.getClientId() == null ||
                dto.getWorkoutSessionId() == null ||
                dto.getVisitTime() == null ||
                dto.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        final VisitDto updated = visitService.updateVisit(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VisitDto> patchVisit(
            @PathVariable final Long id,
            @Valid @RequestBody final VisitDto dto) {


        if (dto.getVisitTime() != null && dto.getVisitTime().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(null);
        }

        final VisitDto updated = visitService.updateVisit(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/attendance")
    public ResponseEntity<VisitDto> markAttendance(
            @PathVariable final Long id,
            @RequestParam final boolean attended) {
        final VisitDto updated = visitService.markAttendance(id, attended);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<VisitDto> cancelBooking(@PathVariable final Long id) {
        final VisitDto cancelled = visitService.cancelBooking(id);
        return cancelled != null ? ResponseEntity.ok(cancelled) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisit(@PathVariable final Long id) {
        final boolean deleted = visitService.deleteVisit(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

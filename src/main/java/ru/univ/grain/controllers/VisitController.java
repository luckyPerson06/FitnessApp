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
        return ResponseEntity.ok(visitService.getAllVisits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitDto> getVisitById(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<VisitDto>> getClientVisits(@PathVariable Long clientId) {
        return ResponseEntity.ok(visitService.getClientVisits(clientId));
    }

    @GetMapping("/client/{clientId}/upcoming")
    public ResponseEntity<List<VisitDto>> getClientUpcomingVisits(@PathVariable Long clientId) {
        return ResponseEntity.ok(visitService.getClientUpcomingVisits(clientId));
    }

    @GetMapping("/client/{clientId}/history")
    public ResponseEntity<List<VisitDto>> getClientHistory(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(visitService.getClientHistory(clientId, from, to));
    }

    @GetMapping("/client/{clientId}/count")
    public ResponseEntity<Long> getClientVisitsCount(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(visitService.getClientVisitsCount(clientId, from, to));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<VisitDto>> getScheduleVisits(@PathVariable Long sessionId) {
        return ResponseEntity.ok(visitService.getScheduleVisits(sessionId));
    }

    @GetMapping("/today")
    public ResponseEntity<List<VisitDto>> getTodayVisits() {
        return ResponseEntity.ok(visitService.getTodayVisits());
    }

    @GetMapping("/subscription/{subscriptionId}/used")
    public ResponseEntity<Long> getSubscriptionUsedVisits(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(visitService.getSubscriptionUsedVisits(subscriptionId));
    }

    @GetMapping("/stats/hourly")
    public ResponseEntity<List<Object[]>> getVisitsByHourStats() {
        return ResponseEntity.ok(visitService.getVisitsByHourStats());
    }

    @PostMapping
    public ResponseEntity<VisitDto> createVisit(@Valid @RequestBody VisitDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.createVisit(dto));
    }

    @PostMapping("/book")
    public ResponseEntity<VisitDto> bookWorkout(
            @RequestParam Long clientId,
            @RequestParam Long sessionId,
            @RequestParam Long subscriptionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.bookWorkout(clientId, sessionId, subscriptionId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitDto> updateVisit(
            @PathVariable Long id,
            @Valid @RequestBody VisitDto dto) {
        return ResponseEntity.ok(visitService.updateVisit(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VisitDto> patchVisit(
            @PathVariable Long id,
            @Valid @RequestBody VisitDto dto) {
        return ResponseEntity.ok(visitService.updateVisit(id, dto));
    }

    @PatchMapping("/{id}/attendance")
    public ResponseEntity<VisitDto> markAttendance(
            @PathVariable Long id,
            @RequestParam boolean attended) {
        return ResponseEntity.ok(visitService.markAttendance(id, attended));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<VisitDto> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.cancelBooking(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.noContent().build();
    }
}

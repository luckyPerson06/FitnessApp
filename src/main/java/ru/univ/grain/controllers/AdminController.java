package ru.univ.grain.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.services.VisitService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Администрирование", description = "Функции администратора")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final VisitService visitService;

    @Operation(summary = "Отметить посещение (пришёл/не пришёл)")
    @PatchMapping("/visits/{visitId}/attendance")
    public ResponseEntity<VisitDto> markAttendance(
            @PathVariable final Long visitId,
            @Parameter(description = "Пришёл ли клиент") @RequestParam final boolean attended) {
        return ResponseEntity.ok(visitService.markAttendance(visitId, attended));
    }

    @Operation(summary = "Получить посещения за день")
    @GetMapping("/visits/by-date")
    public ResponseEntity<List<VisitDto>> getVisitsByDate(
            @Parameter(description = "Дата", example = "2026-04-14")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date) {
        return ResponseEntity.ok(visitService.getVisitsByDate(date));
    }

    @Operation(summary = "Получить статистику посещаемости по тренировке")
    @GetMapping("/sessions/{sessionId}/attendance-stats")
    public ResponseEntity<VisitService.AttendanceStats> getAttendanceStats(@PathVariable final Long sessionId) {
        return ResponseEntity.ok(visitService.getAttendanceStats(sessionId));
    }
}

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
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.services.VisitService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Посещения", description = "Управление посещениями и записью на тренировки")
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @Operation(summary = "Получить все посещения")
    @GetMapping
    public ResponseEntity<List<VisitDto>> getAllVisits() {
        return ResponseEntity.ok(visitService.getAllVisits());
    }

    @Operation(summary = "Получить посещение по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посещение найдено"),
            @ApiResponse(responseCode = "404", description = "Посещение не найдено", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<VisitDto> getVisitById(@Parameter(description = "ID посещения", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    @Operation(summary = "Получить все посещения клиента")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<VisitDto>> getClientVisits(@Parameter(description = "ID клиента", example = "1") @PathVariable Long clientId) {
        return ResponseEntity.ok(visitService.getClientVisits(clientId));
    }

    @Operation(summary = "Получить предстоящие посещения клиента")
    @GetMapping("/client/{clientId}/upcoming")
    public ResponseEntity<List<VisitDto>> getClientUpcomingVisits(@Parameter(description = "ID клиента", example = "1") @PathVariable Long clientId) {
        return ResponseEntity.ok(visitService.getClientUpcomingVisits(clientId));
    }

    @Operation(summary = "Получить историю посещений клиента за период")
    @GetMapping("/client/{clientId}/history")
    public ResponseEntity<List<VisitDto>> getClientHistory(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long clientId,
            @Parameter(description = "Дата начала", example = "2026-03-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Дата окончания", example = "2026-03-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(visitService.getClientHistory(clientId, from, to));
    }

    @Operation(summary = "Получить количество посещений клиента за период")
    @GetMapping("/client/{clientId}/count")
    public ResponseEntity<Long> getClientVisitsCount(
            @Parameter(description = "ID клиента", example = "1") @PathVariable Long clientId,
            @Parameter(description = "Дата начала", example = "2026-03-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Дата окончания", example = "2026-03-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(visitService.getClientVisitsCount(clientId, from, to));
    }

    @Operation(summary = "Получить посещения по тренировке")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<VisitDto>> getScheduleVisits(@Parameter(description = "ID тренировки", example = "1") @PathVariable Long sessionId) {
        return ResponseEntity.ok(visitService.getScheduleVisits(sessionId));
    }

    @Operation(summary = "Получить сегодняшние посещения")
    @GetMapping("/today")
    public ResponseEntity<List<VisitDto>> getTodayVisits() {
        return ResponseEntity.ok(visitService.getTodayVisits());
    }

    @Operation(summary = "Получить количество использованных посещений по абонементу")
    @GetMapping("/subscription/{subscriptionId}/used")
    public ResponseEntity<Long> getSubscriptionUsedVisits(@Parameter(description = "ID абонемента", example = "1") @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(visitService.getSubscriptionUsedVisits(subscriptionId));
    }

    @Operation(summary = "Получить статистику посещений по часам")
    @GetMapping("/stats/hourly")
    public ResponseEntity<List<Object[]>> getVisitsByHourStats() {
        return ResponseEntity.ok(visitService.getVisitsByHourStats());
    }

    @Operation(summary = "Создать новое посещение")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Посещение создано"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<VisitDto> createVisit(@Valid @RequestBody VisitDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.createVisit(dto));
    }

    @Operation(summary = "Записаться на тренировку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Успешная запись"),
            @ApiResponse(responseCode = "400", description = "Ошибка: нет мест, уже записан, абонемент не подходит", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Клиент, тренировка или абонемент не найдены", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/book")
    public ResponseEntity<VisitDto> bookWorkout(
            @Parameter(description = "ID клиента", example = "1") @RequestParam Long clientId,
            @Parameter(description = "ID тренировки", example = "1") @RequestParam Long sessionId,
            @Parameter(description = "ID абонемента", example = "1") @RequestParam Long subscriptionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.bookWorkout(clientId, sessionId, subscriptionId));
    }

    @Operation(summary = "Обновить посещение")
    @PutMapping("/{id}")
    public ResponseEntity<VisitDto> updateVisit(
            @Parameter(description = "ID посещения", example = "1") @PathVariable Long id,
            @Valid @RequestBody VisitDto dto) {
        return ResponseEntity.ok(visitService.updateVisit(id, dto));
    }

    @Operation(summary = "Частичное обновление посещения")
    @PatchMapping("/{id}")
    public ResponseEntity<VisitDto> patchVisit(
            @Parameter(description = "ID посещения", example = "1") @PathVariable Long id,
            @Valid @RequestBody VisitDto dto) {
        return ResponseEntity.ok(visitService.updateVisit(id, dto));
    }

    @Operation(summary = "Отметить посещение (пришёл/не пришёл)")
    @PatchMapping("/{id}/attendance")
    public ResponseEntity<VisitDto> markAttendance(
            @Parameter(description = "ID посещения", example = "1") @PathVariable Long id,
            @Parameter(description = "Пришёл ли клиент", example = "true") @RequestParam boolean attended) {
        return ResponseEntity.ok(visitService.markAttendance(id, attended));
    }

    @Operation(summary = "Отменить запись на тренировку")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<VisitDto> cancelBooking(@Parameter(description = "ID посещения", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(visitService.cancelBooking(id));
    }

    @Operation(summary = "Удалить посещение")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Посещение удалено"),
            @ApiResponse(responseCode = "404", description = "Посещение не найдено", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisit(@Parameter(description = "ID посещения", example = "1") @PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.noContent().build();
    }
}

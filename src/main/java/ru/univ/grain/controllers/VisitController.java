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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.entities.Client;
import ru.univ.grain.entities.User;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.repositories.ClientRepository;
import ru.univ.grain.services.VisitService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Посещения", description = "Управление посещениями и записью на тренировки")
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;
    private final ClientRepository clientRepository;

    @Operation(summary = "Получить все посещения")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitDto>> getAllVisits() {
        return ResponseEntity.ok(visitService.getAllVisits());
    }

    @Operation(summary = "Получить посещение по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посещение найдено"),
            @ApiResponse(responseCode = "404", description = "Посещение не найден",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VisitDto> getVisitById(
            @Parameter(description = "ID посещения", example = "1") @PathVariable final Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    @Operation(summary = "Получить все посещения клиента")
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitDto>> getClientVisits(
            @Parameter(description = "ID клиента", example = "1") @PathVariable final Long clientId) {
        return ResponseEntity.ok(visitService.getClientVisits(clientId));
    }

    @Operation(summary = "Получить предстоящие посещения клиента")
    @GetMapping("/client/{clientId}/upcoming")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitDto>> getClientUpcomingVisits(
            @Parameter(description = "ID клиента", example = "1") @PathVariable final Long clientId) {
        return ResponseEntity.ok(visitService.getClientUpcomingVisits(clientId));
    }

    @Operation(summary = "Получить историю посещений клиента за период")
    @GetMapping("/client/{clientId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitDto>> getClientHistory(
            @Parameter(description = "ID клиента", example = "1") @PathVariable final Long clientId,
            @Parameter(description = "Дата начала", example = "2026-03-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @Parameter(description = "Дата окончания", example = "2026-03-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return ResponseEntity.ok(visitService.getClientHistory(clientId, from, to));
    }

    @Operation(summary = "Получить количество посещений клиента за период")
    @GetMapping("/client/{clientId}/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getClientVisitsCount(
            @Parameter(description = "ID клиента", example = "1") @PathVariable final Long clientId,
            @Parameter(description = "Дата начала", example = "2026-03-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @Parameter(description = "Дата окончания", example = "2026-03-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return ResponseEntity.ok(visitService.getClientVisitsCount(clientId, from, to));
    }

    @Operation(summary = "Получить посещения по тренировке")
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitDto>> getScheduleVisits(
            @Parameter(description = "ID тренировки", example = "1") @PathVariable final Long sessionId) {
        return ResponseEntity.ok(visitService.getScheduleVisits(sessionId));
    }

    @Operation(summary = "Получить сегодняшние посещения")
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VisitDto>> getTodayVisits() {
        return ResponseEntity.ok(visitService.getTodayVisits());
    }

    @Operation(summary = "Создать новое посещение")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Посещение создано"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VisitDto> createVisit(@Valid @RequestBody final VisitDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.createVisit(dto));
    }

    @Operation(summary = "Обновить посещение")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VisitDto> updateVisit(
            @Parameter(description = "ID посещения", example = "1") @PathVariable final Long id,
            @Valid @RequestBody final VisitDto dto) {
        return ResponseEntity.ok(visitService.updateVisit(id, dto));
    }

    @Operation(summary = "Частичное обновление посещения")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VisitDto> patchVisit(
            @Parameter(description = "ID посещения", example = "1") @PathVariable final Long id,
            @Valid @RequestBody final VisitDto dto) {
        return ResponseEntity.ok(visitService.updateVisit(id, dto));
    }

    @Operation(summary = "Отметить посещение (пришёл/не пришёл)")
    @PatchMapping("/{id}/attendance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VisitDto> markAttendance(
            @Parameter(description = "ID посещения", example = "1") @PathVariable final Long id,
            @Parameter(description = "Пришёл ли клиент", example = "true") @RequestParam final boolean attended) {
        return ResponseEntity.ok(visitService.markAttendance(id, attended));
    }

    @Operation(summary = "Удалить посещение")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Посещение удалено"),
            @ApiResponse(responseCode = "404", description = "Посещение не найдено")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVisit(
            @Parameter(description = "ID посещения", example = "1") @PathVariable final Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Записаться на тренировку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Успешная запись"),
            @ApiResponse(responseCode = "400", description = "Ошибка: нет мест, уже записан, абонемент не подходит"),
            @ApiResponse(responseCode = "404", description = "Клиент, тренировка или абонемент не найдены")
    })
    @PostMapping("/book")
    public ResponseEntity<VisitDto> bookWorkout(
            @AuthenticationPrincipal final User user,
            @Parameter(description = "ID тренировки", example = "1") @RequestParam final Long sessionId,
            @Parameter(description = "ID абонемента", example = "1") @RequestParam final Long subscriptionId) {
        final Client client = clientRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(visitService.bookWorkout(client.getId(), sessionId, subscriptionId));
    }

    @Operation(summary = "Отменить запись на тренировку")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<VisitDto> cancelBooking(
            @AuthenticationPrincipal final User user,
            @Parameter(description = "ID посещения", example = "1") @PathVariable final Long id) {
        final Client client = clientRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден"));
        final VisitDto visit = visitService.getVisitById(id);
        if (!visit.getClientId().equals(client.getId())) {
            throw new ResourceNotFoundException("Запись не найдена или не принадлежит вам");
        }
        return ResponseEntity.ok(visitService.cancelBooking(id));
    }
}

package ru.univ.grain.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.ProfileResponse;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.entities.User;
import ru.univ.grain.services.ProfileService;

import java.util.List;

@Tag(name = "Профиль", description = "Личный кабинет клиента")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Получить профиль текущего пользователя")
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal final User user) {
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @Operation(summary = "Получить предстоящие тренировки")
    @GetMapping("/visits/upcoming")
    public ResponseEntity<List<VisitDto>> getUpcomingVisits(@AuthenticationPrincipal final User user) {
        return ResponseEntity.ok(profileService.getUpcomingVisits(user));
    }

    @Operation(summary = "Получить историю посещений")
    @GetMapping("/visits/history")
    public ResponseEntity<List<VisitDto>> getVisitHistory(@AuthenticationPrincipal final User user) {
        return ResponseEntity.ok(profileService.getVisitHistory(user));
    }

    @Operation(summary = "Отменить запись на тренировку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запись отменена"),
            @ApiResponse(responseCode = "400", description = "Невозможно отменить",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/visits/{visitId}/cancel")
    public ResponseEntity<VisitDto> cancelVisit(
            @AuthenticationPrincipal final User user,
            @PathVariable final Long visitId) {
        return ResponseEntity.ok(profileService.cancelVisit(user, visitId));
    }
}

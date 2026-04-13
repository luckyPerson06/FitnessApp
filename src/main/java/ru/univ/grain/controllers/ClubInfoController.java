package ru.univ.grain.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.ApiError;
import ru.univ.grain.dto.ClubInfoDto;
import ru.univ.grain.services.ClubInfoService;

@Tag(name = "Информация о клубе", description = "Получение и редактирование информации о клубе")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClubInfoController {

    private final ClubInfoService clubInfoService;

    @Operation(summary = "Получить информацию о клубе")
    @GetMapping("/club-info")
    public ResponseEntity<ClubInfoDto> getClubInfo() {
        return ResponseEntity.ok(clubInfoService.getClubInfo());
    }

    @Operation(summary = "Обновить информацию о клубе (только админ)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация обновлена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/admin/club-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClubInfoDto> updateClubInfo(@Valid @RequestBody final ClubInfoDto dto) {
        return ResponseEntity.ok(clubInfoService.updateClubInfo(dto));
    }
}

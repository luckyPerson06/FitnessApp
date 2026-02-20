package ru.univ.grain.controllers;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import ru.univ.grain.domain.Client;
import ru.univ.grain.dto.ClientCreateDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.dto.ClientUpdateDto;
import ru.univ.grain.mapper.ClientMapper;
import ru.univ.grain.services.ClientService;

@AllArgsConstructor
@RestController
@RequestMapping("/clients")
public class ClientController {
    private final ClientService service;
    private final ClientMapper mapper;

    @GetMapping("/{id}")
    public ClientResponseDto getClientById(@PathVariable long id) {
        return mapper.toDto(service.getClientById(id));
    }

    @GetMapping("/search")
    public List<ClientResponseDto> getClientByLastName(@RequestParam String lastName) {
        return service.getClientByLastName(lastName)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping
    public ClientResponseDto createClient(@RequestBody ClientCreateDto dto) {
        final Client client = mapper.toEntity(dto, 0);
        final Client saved = service.createClient(client);
        return mapper.toDto(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable long id) {
        service.deleteClientById(id);
    }

    @PatchMapping("/{id}")
    public ClientResponseDto  patchClient(@PathVariable long id, @RequestBody ClientPatchDto dto) {
        final Client patched = service.patch(id, dto);
        return mapper.toDto(patched);
    }

    @PutMapping("/{id}")
    public ClientResponseDto  updateClient(
            @PathVariable long id,
            @RequestBody ClientUpdateDto dto) {
        final Client updated = service.updateClient(id, dto);
        return mapper.toDto(updated);
    }
}

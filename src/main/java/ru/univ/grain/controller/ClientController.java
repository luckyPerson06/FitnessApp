package ru.univ.grain.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import ru.univ.grain.domain.Client;
import ru.univ.grain.dto.ClientCreateDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.mapper.ClientMapper;
import ru.univ.grain.service.ClientService;


@RestController
@RequestMapping("/clients")
public class ClientController {
    private final ClientService service;

    private final ClientMapper mapper;

    public ClientController(ClientService service, ClientMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ClientResponseDto getClientById(@PathVariable long id) {
        return mapper.toDto(service.getClientById(id));
    }

    @GetMapping("/search")
    public List<ClientResponseDto> getByLastName(@RequestParam String lastName) {
        return service.findByLastName(lastName)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping
    public ClientResponseDto createClient(@RequestBody ClientCreateDto dto) {
        Client client = mapper.toEntity(dto, 0);
        Client saved = service.createClient(client);
        return mapper.toDto(saved);
    }

}

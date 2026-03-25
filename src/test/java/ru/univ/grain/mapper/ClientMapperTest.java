package ru.univ.grain.mapper;

import org.junit.jupiter.api.Test;
import ru.univ.grain.dto.ClientDto;
import ru.univ.grain.dto.ClientPatchDto;
import ru.univ.grain.dto.ClientResponseDto;
import ru.univ.grain.entities.Client;
import ru.univ.grain.entities.ClientStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ClientMapperTest {

    private final ClientMapper clientMapper = new ClientMapperImpl();

    @Test
    void toResponseDto_ShouldMapFullName_WhenMiddleNameExists() {
        Client client = Client.builder()
                .id(1L)
                .firstName("Иван")
                .middleName("Иванович")
                .lastName("Иванов")
                .email("ivan@mail.com")
                .phoneNumber("+79991234567")
                .status(ClientStatus.ACTIVE)
                .build();

        ClientResponseDto result = clientMapper.toResponseDto(client);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("Иванов Иван Иванович");
        assertThat(result.getEmail()).isEqualTo("ivan@mail.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+79991234567");
        assertThat(result.getStatus()).isEqualTo(ClientStatus.ACTIVE);
    }

    @Test
    void toResponseDto_ShouldMapFullName_WhenMiddleNameNull() {
        Client client = Client.builder()
                .firstName("Иван")
                .middleName(null)
                .lastName("Иванов")
                .build();

        ClientResponseDto result = clientMapper.toResponseDto(client);

        assertThat(result.getFullName()).isEqualTo("Иванов Иван");
    }

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        ClientDto dto = ClientDto.builder()
                .firstName("Иван")
                .middleName("Иванович")
                .lastName("Иванов")
                .email("ivan@mail.com")
                .phoneNumber("+79991234567")
                .password("password123")
                .build();

        Client result = clientMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getFirstName()).isEqualTo("Иван");
        assertThat(result.getMiddleName()).isEqualTo("Иванович");
        assertThat(result.getLastName()).isEqualTo("Иванов");
        assertThat(result.getEmail()).isEqualTo("ivan@mail.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+79991234567");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.getStatus()).isEqualTo(ClientStatus.ACTIVE);
        assertThat(result.getSubscriptions()).isNull();
        assertThat(result.getVisits()).isNull();
    }

    @Test
    void updateEntity_ShouldUpdateOnlyNonNullFields() {
        Client client = Client.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .email("old@mail.com")
                .status(ClientStatus.ACTIVE)
                .build();

        ClientPatchDto patchDto = ClientPatchDto.builder()
                .firstName("Анна")
                .phoneNumber("+79998887766")
                .build();

        clientMapper.updateEntity(patchDto, client);

        assertThat(client.getFirstName()).isEqualTo("Анна");
        assertThat(client.getLastName()).isEqualTo("Иванов");
        assertThat(client.getEmail()).isEqualTo("old@mail.com");
        assertThat(client.getPhoneNumber()).isEqualTo("+79998887766");
        assertThat(client.getStatus()).isEqualTo(ClientStatus.ACTIVE);
    }

    @Test
    void updateEntity_ShouldIgnoreNullFields() {
        Client client = Client.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .email("old@mail.com")
                .phoneNumber("+79991234567")
                .build();

        ClientPatchDto patchDto = ClientPatchDto.builder()
                .firstName(null)
                .lastName(null)
                .build();

        clientMapper.updateEntity(patchDto, client);

        assertThat(client.getFirstName()).isEqualTo("Иван");
        assertThat(client.getLastName()).isEqualTo("Иванов");
    }
}
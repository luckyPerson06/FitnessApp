package ru.univ.grain.mapper;

import org.junit.jupiter.api.Test;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.entities.*;
import ru.univ.grain.entities.Visit;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VisitMapperTest {

    private final VisitMapper visitMapper = new VisitMapperImpl();

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        VisitDto dto = VisitDto.builder()
                .clientId(1L)
                .workoutSessionId(1L)
                .subscriptionId(1L)
                .visitTime(LocalDateTime.of(2026, 3, 26, 10, 0))
                .build();

        Visit result = visitMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getVisitTime()).isEqualTo(LocalDateTime.of(2026, 3, 26, 10, 0));
        assertThat(result.getStatus()).isEqualTo(VisitStatus.BOOKED);
        assertThat(result.getVersion()).isNull();
    }

    @Test
    void toDto_ShouldMapEntityToDto() {
        Client client = Client.builder().id(1L).build();
        WorkoutSession session = WorkoutSession.builder().id(1L).build();
        Subscription subscription = Subscription.builder().id(1L).build();

        Visit visit = Visit.builder()
                .id(1L)
                .client(client)
                .workoutSession(session)
                .subscription(subscription)
                .visitTime(LocalDateTime.of(2026, 3, 26, 10, 0))
                .status(VisitStatus.ATTENDED)
                .build();

        VisitDto result = visitMapper.toDto(visit);

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(1L);
        assertThat(result.getWorkoutSessionId()).isEqualTo(1L);
        assertThat(result.getSubscriptionId()).isEqualTo(1L);
        assertThat(result.getVisitTime()).isEqualTo(LocalDateTime.of(2026, 3, 26, 10, 0));
        assertThat(result.getStatus()).isEqualTo(VisitStatus.ATTENDED);
    }

    @Test
    void toDto_ShouldHandleNullSubscription() {
        Client client = Client.builder().id(1L).build();
        WorkoutSession session = WorkoutSession.builder().id(1L).build();

        Visit visit = Visit.builder()
                .id(1L)
                .client(client)
                .workoutSession(session)
                .subscription(null)
                .visitTime(LocalDateTime.now())
                .status(VisitStatus.BOOKED)
                .build();

        VisitDto result = visitMapper.toDto(visit);

        assertThat(result.getSubscriptionId()).isNull();
    }

    @Test
    void updateEntity_ShouldUpdateOnlyNonNullFields() {
        Visit visit = Visit.builder()
                .id(1L)
                .status(VisitStatus.BOOKED)
                .build();

        VisitDto dto = VisitDto.builder()
                .status(VisitStatus.CANCELLED)
                .build();

        visitMapper.updateEntity(dto, visit);

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.CANCELLED);
    }
}
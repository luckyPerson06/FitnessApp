package ru.univ.grain.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.entities.*;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.VisitMapper;
import ru.univ.grain.repositories.ClientRepository;
import ru.univ.grain.repositories.SubscriptionRepository;
import ru.univ.grain.repositories.VisitRepository;
import ru.univ.grain.repositories.WorkoutSessionRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private VisitMapper visitMapper;

    @InjectMocks
    private VisitService visitService;

    @Test
    void getAllVisits_ShouldReturnList() {
        Visit visit1 = new Visit();
        Visit visit2 = new Visit();
        VisitDto dto1 = new VisitDto();
        VisitDto dto2 = new VisitDto();

        when(visitRepository.findAll()).thenReturn(List.of(visit1, visit2));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(dto1, dto2);

        List<VisitDto> result = visitService.getAllVisits();

        assertThat(result).hasSize(2);
    }

    @Test
    void getVisitById_ShouldReturnVisit_WhenExists() {
        Long id = 1L;
        Visit visit = new Visit();
        visit.setId(id);
        VisitDto dto = new VisitDto();

        when(visitRepository.findById(id)).thenReturn(Optional.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(dto);

        VisitDto result = visitService.getVisitById(id);

        assertThat(result).isNotNull();
    }

    @Test
    void getVisitById_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(visitRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.getVisitById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void createVisit_ShouldReturnVisit_WhenValid() {
        VisitDto dto = new VisitDto();
        dto.setClientId(1L);
        dto.setWorkoutSessionId(1L);
        dto.setSubscriptionId(1L);

        Client client = new Client();
        client.setId(1L);

        WorkoutSession session = new WorkoutSession();
        session.setId(1L);

        Subscription subscription = new Subscription();
        subscription.setId(1L);

        Visit visit = new Visit();
        visit.setId(1L);

        VisitDto responseDto = new VisitDto();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(visitMapper.toEntity(dto)).thenReturn(visit);
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);

        VisitDto result = visitService.createVisit(dto);

        assertThat(result).isNotNull();
        verify(visitRepository).save(any(Visit.class));
    }

    @Test
    void createVisit_ShouldThrowException_WhenClientNotFound() {
        VisitDto dto = new VisitDto();
        dto.setClientId(999L);
        dto.setWorkoutSessionId(1L);

        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.createVisit(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void createVisit_ShouldThrowException_WhenSessionNotFound() {
        VisitDto dto = new VisitDto();
        dto.setClientId(1L);
        dto.setWorkoutSessionId(999L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(new Client()));
        when(workoutSessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.createVisit(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найдена");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void updateVisit_ShouldUpdateVisit_WhenValid() {
        Long id = 1L;
        VisitDto dto = new VisitDto();
        dto.setClientId(1L);
        dto.setWorkoutSessionId(1L);

        Visit existing = new Visit();
        existing.setId(id);

        Client client = new Client();
        client.setId(1L);

        WorkoutSession session = new WorkoutSession();
        session.setId(1L);

        Visit updated = new Visit();
        updated.setId(id);

        VisitDto responseDto = new VisitDto();

        when(visitRepository.findById(id)).thenReturn(Optional.of(existing));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(visitRepository.save(any(Visit.class))).thenReturn(updated);
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);

        VisitDto result = visitService.updateVisit(id, dto);

        assertThat(result).isNotNull();
        verify(visitMapper).updateEntity(dto, existing);
        verify(visitRepository).save(existing);
    }

    @Test
    void updateVisit_ShouldThrowException_WhenNotFound() {
        Long id = 999L;
        VisitDto dto = new VisitDto();
        dto.setClientId(1L);
        dto.setWorkoutSessionId(1L);

        when(visitRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.updateVisit(id, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void deleteVisit_ShouldDeleteVisit_WhenExists() {
        Long id = 1L;
        Visit visit = new Visit();
        visit.setId(id);

        when(visitRepository.findById(id)).thenReturn(Optional.of(visit));

        visitService.deleteVisit(id);

        verify(visitRepository).delete(visit);
    }

    @Test
    void deleteVisit_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(visitRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.deleteVisit(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(visitRepository, never()).delete(any());
    }

    @Test
    void bookWorkout_ShouldReturnVisit_WhenValid() {
        Long clientId = 1L;
        Long sessionId = 1L;
        Long subscriptionId = 1L;

        Client client = new Client();
        client.setId(clientId);

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        List<WorkoutType> allowed = new ArrayList<>();
        allowed.add(workoutType);

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        subscription.setAllowedWorkoutTypes(allowed);

        WorkoutSession session = new WorkoutSession();
        session.setId(sessionId);
        session.setStatus(WorkoutSessionStatus.SCHEDULED);
        session.setWorkoutType(workoutType);
        session.setMaxParticipants(10);
        session.setDayOfWeek(DayOfWeek.MONDAY);
        session.setStartTime(LocalTime.of(10, 0));

        Visit visit = new Visit();
        visit.setId(1L);

        VisitDto responseDto = new VisitDto();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(visitRepository.findBookedVisitsByClient(clientId)).thenReturn(new ArrayList<>());
        when(visitRepository.findBookedVisitsBySession(sessionId)).thenReturn(new ArrayList<>());
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);

        VisitDto result = visitService.bookWorkout(clientId, sessionId, subscriptionId);

        assertThat(result).isNotNull();
        verify(visitRepository).save(any(Visit.class));
    }

    @Test
    void bookWorkout_ShouldThrowException_WhenClientNotFound() {
        Long clientId = 999L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.bookWorkout(clientId, 1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookWorkout_ShouldThrowException_WhenSessionNotFound() {
        Long sessionId = 999L;

        when(clientRepository.findById(1L)).thenReturn(Optional.of(new Client()));
        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.bookWorkout(1L, sessionId, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найдена");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookWorkout_ShouldThrowException_WhenSessionNotAvailable() {
        Long sessionId = 1L;
        WorkoutSession session = new WorkoutSession();
        session.setStatus(WorkoutSessionStatus.COMPLETED);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(new Client()));
        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(new Subscription()));

        assertThatThrownBy(() -> visitService.bookWorkout(1L, sessionId, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("недоступна");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookWorkout_ShouldThrowException_WhenWorkoutTypeNotAllowed() {
        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        Subscription subscription = new Subscription();
        subscription.setAllowedWorkoutTypes(new ArrayList<>());

        WorkoutSession session = new WorkoutSession();
        session.setStatus(WorkoutSessionStatus.SCHEDULED);
        session.setWorkoutType(workoutType);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(new Client()));
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> visitService.bookWorkout(1L, 1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("не подходит");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookWorkout_ShouldThrowException_WhenAlreadyBooked() {
        Long clientId = 1L;
        Long sessionId = 1L;

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        List<WorkoutType> allowed = new ArrayList<>();
        allowed.add(workoutType);

        Subscription subscription = new Subscription();
        subscription.setAllowedWorkoutTypes(allowed);

        WorkoutSession session = new WorkoutSession();
        session.setId(sessionId);
        session.setStatus(WorkoutSessionStatus.SCHEDULED);
        session.setWorkoutType(workoutType);

        Visit existingVisit = new Visit();
        existingVisit.setWorkoutSession(session);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(new Client()));
        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(visitRepository.findBookedVisitsByClient(clientId)).thenReturn(List.of(existingVisit));

        assertThatThrownBy(() -> visitService.bookWorkout(clientId, sessionId, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже записаны");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void bookWorkout_ShouldThrowException_WhenNoAvailableSpots() {
        Long clientId = 1L;
        Long sessionId = 1L;

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        List<WorkoutType> allowed = new ArrayList<>();
        allowed.add(workoutType);

        Subscription subscription = new Subscription();
        subscription.setAllowedWorkoutTypes(allowed);

        WorkoutSession session = new WorkoutSession();
        session.setId(sessionId);
        session.setStatus(WorkoutSessionStatus.SCHEDULED);
        session.setWorkoutType(workoutType);
        session.setMaxParticipants(5);

        List<Visit> bookedVisits = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            bookedVisits.add(new Visit());
        }

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(new Client()));
        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(visitRepository.findBookedVisitsByClient(clientId)).thenReturn(new ArrayList<>());
        when(visitRepository.findBookedVisitsBySession(sessionId)).thenReturn(bookedVisits);

        assertThatThrownBy(() -> visitService.bookWorkout(clientId, sessionId, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Нет свободных мест");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void markAttendance_ShouldUpdateStatus_WhenValid() {
        Long visitId = 1L;
        Visit visit = new Visit();
        visit.setId(visitId);
        visit.setStatus(VisitStatus.BOOKED);

        VisitDto responseDto = new VisitDto();

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        VisitDto result = visitService.markAttendance(visitId, true);

        assertThat(result).isNotNull();
        assertThat(visit.getStatus()).isEqualTo(VisitStatus.ATTENDED);
        verify(visitRepository).save(visit);
    }

    @Test
    void markAttendance_ShouldThrowException_WhenNotBooked() {
        Long visitId = 1L;
        Visit visit = new Visit();
        visit.setId(visitId);
        visit.setStatus(VisitStatus.COMPLETED);

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> visitService.markAttendance(visitId, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Неверный статус");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void cancelBooking_ShouldUpdateStatus_WhenValid() {
        Long visitId = 1L;
        Visit visit = new Visit();
        visit.setId(visitId);
        visit.setStatus(VisitStatus.BOOKED);

        VisitDto responseDto = new VisitDto();

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        VisitDto result = visitService.cancelBooking(visitId);

        assertThat(result).isNotNull();
        assertThat(visit.getStatus()).isEqualTo(VisitStatus.CANCELLED);
        verify(visitRepository).save(visit);
    }

    @Test
    void getClientVisits_ShouldReturnList() {
        Long clientId = 1L;
        Visit visit = new Visit();

        when(visitRepository.findByClientId(clientId)).thenReturn(List.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(new VisitDto());

        List<VisitDto> result = visitService.getClientVisits(clientId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getClientUpcomingVisits_ShouldReturnList() {
        Long clientId = 1L;
        Visit visit = new Visit();
        visit.setVisitTime(LocalDateTime.now().plusDays(1));

        when(visitRepository.findBookedVisitsByClient(clientId)).thenReturn(List.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(new VisitDto());

        List<VisitDto> result = visitService.getClientUpcomingVisits(clientId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getClientHistory_ShouldReturnFilteredList() {
        Long clientId = 1L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        Visit visit = new Visit();
        Client client = new Client();
        client.setId(clientId);
        visit.setClient(client);

        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(new VisitDto());

        List<VisitDto> result = visitService.getClientHistory(clientId, from, to);

        assertThat(result).hasSize(1);
    }

    @Test
    void getScheduleVisits_ShouldReturnList() {
        Long scheduleId = 1L;
        Visit visit = new Visit();

        when(visitRepository.findByWorkoutSessionId(scheduleId)).thenReturn(List.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(new VisitDto());

        List<VisitDto> result = visitService.getScheduleVisits(scheduleId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTodayVisits_ShouldReturnList() {
        Visit visit = new Visit();

        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(new VisitDto());

        List<VisitDto> result = visitService.getTodayVisits();

        assertThat(result).hasSize(1);
    }

    @Test
    void getClientVisitsCount_ShouldReturnCount() {
        Long clientId = 1L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        Visit visit = new Visit();
        Client client = new Client();
        client.setId(clientId);
        visit.setClient(client);
        visit.setStatus(VisitStatus.ATTENDED);

        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of(visit));

        long result = visitService.getClientVisitsCount(clientId, from, to);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void getSubscriptionUsedVisits_ShouldReturnCount() {
        Long subscriptionId = 1L;

        when(visitRepository.countAttendedBySubscriptionId(subscriptionId)).thenReturn(5L);

        long result = visitService.getSubscriptionUsedVisits(subscriptionId);

        assertThat(result).isEqualTo(5);
    }

    @Test
    void getVisitsByHourStats_ShouldReturnStats() {
        List<Object[]> stats = new ArrayList<>();
        stats.add(new Object[]{10, 5L});

        when(visitRepository.getVisitsByHour()).thenReturn(stats);

        List<Object[]> result = visitService.getVisitsByHourStats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)[0]).isEqualTo(10);
        assertThat(result.get(0)[1]).isEqualTo(5L);
    }

    @Test
    void getAllVisits_ShouldReturnEmptyList_WhenNoVisits() {
        when(visitRepository.findAll()).thenReturn(List.of());

        List<VisitDto> result = visitService.getAllVisits();

        assertThat(result).isEmpty();
    }


    @Test
    void createVisit_ShouldCreate_WhenSubscriptionIdIsNull() {
        VisitDto dto = new VisitDto();
        dto.setClientId(1L);
        dto.setWorkoutSessionId(1L);
        dto.setSubscriptionId(null);

        Client client = new Client();
        WorkoutSession session = new WorkoutSession();
        Visit visit = new Visit();
        VisitDto responseDto = new VisitDto();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(visitMapper.toEntity(dto)).thenReturn(visit);
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);

        VisitDto result = visitService.createVisit(dto);

        assertThat(result).isNotNull();
        verify(subscriptionRepository, never()).findById(any());
    }


    @Test
    void bookWorkout_ShouldThrowException_WhenSubscriptionNotFound() {
        Long clientId = 1L;
        Long sessionId = 1L;
        Long subscriptionId = 999L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(new Client()));
        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(new WorkoutSession()));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.bookWorkout(clientId, sessionId, subscriptionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(visitRepository, never()).save(any());
    }





    @Test
    void getClientVisits_ShouldReturnEmptyList_WhenClientIdIsNull() {
        List<VisitDto> result = visitService.getClientVisits(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getClientVisits_ShouldReturnEmptyList_WhenNoVisits() {
        Long clientId = 1L;

        when(visitRepository.findByClientId(clientId)).thenReturn(List.of());

        List<VisitDto> result = visitService.getClientVisits(clientId);

        assertThat(result).isEmpty();
    }

    @Test
    void getClientUpcomingVisits_ShouldReturnEmptyList_WhenClientIdIsNull() {
        List<VisitDto> result = visitService.getClientUpcomingVisits(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getClientUpcomingVisits_ShouldReturnEmptyList_WhenNoUpcoming() {
        Long clientId = 1L;

        when(visitRepository.findBookedVisitsByClient(clientId)).thenReturn(List.of());

        List<VisitDto> result = visitService.getClientUpcomingVisits(clientId);

        assertThat(result).isEmpty();
    }

    @Test
    void getClientHistory_ShouldReturnEmptyList_WhenClientIdIsNull() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        List<VisitDto> result = visitService.getClientHistory(null, from, to);
        assertThat(result).isEmpty();
    }



    @Test
    void getScheduleVisits_ShouldReturnEmptyList_WhenScheduleIdIsNull() {
        List<VisitDto> result = visitService.getScheduleVisits(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getScheduleVisits_ShouldReturnEmptyList_WhenNoVisits() {
        Long scheduleId = 1L;

        when(visitRepository.findByWorkoutSessionId(scheduleId)).thenReturn(List.of());

        List<VisitDto> result = visitService.getScheduleVisits(scheduleId);

        assertThat(result).isEmpty();
    }

    @Test
    void getTodayVisits_ShouldReturnEmptyList_WhenNoVisits() {
        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of());

        List<VisitDto> result = visitService.getTodayVisits();

        assertThat(result).isEmpty();
    }

    @Test
    void getClientVisitsCount_ShouldReturnZero_WhenClientIdIsNull() {
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        long result = visitService.getClientVisitsCount(null, from, to);
        assertThat(result).isZero();
    }



    @Test
    void getSubscriptionUsedVisits_ShouldReturnZero_WhenSubscriptionIdIsNull() {
        long result = visitService.getSubscriptionUsedVisits(null);
        assertThat(result).isZero();
    }

    @Test
    void getVisitsByHourStats_ShouldReturnEmptyList_WhenNoStats() {
        when(visitRepository.getVisitsByHour()).thenReturn(List.of());

        List<Object[]> result = visitService.getVisitsByHourStats();

        assertThat(result).isEmpty();
    }

    @Test
    void getClientHistory_ShouldReturnEmptyList_WhenNoVisits() {
        Long clientId = 1L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of());

        List<VisitDto> result = visitService.getClientHistory(clientId, from, to);

        assertThat(result).isEmpty();
    }

    @Test
    void getClientHistory_ShouldReturnEmptyList_WhenNoMatchingClient() {
        Long clientId = 999L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        Visit visit = new Visit();
        Client client = new Client();
        client.setId(1L);
        visit.setClient(client);

        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of(visit));

        List<VisitDto> result = visitService.getClientHistory(clientId, from, to);

        assertThat(result).isEmpty();
    }

    @Test
    void getClientVisitsCount_ShouldReturnZero_WhenNoVisits() {
        Long clientId = 1L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of());

        long result = visitService.getClientVisitsCount(clientId, from, to);

        assertThat(result).isZero();
    }


    @Test
    void getClientUpcomingVisits_ShouldFilterOutPastVisits() {
        Long clientId = 1L;

        Visit pastVisit = new Visit();
        pastVisit.setVisitTime(LocalDateTime.now().minusDays(1));
        Visit futureVisit = new Visit();
        futureVisit.setVisitTime(LocalDateTime.now().plusDays(1));

        when(visitRepository.findBookedVisitsByClient(clientId)).thenReturn(List.of(pastVisit, futureVisit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(new VisitDto());

        List<VisitDto> result = visitService.getClientUpcomingVisits(clientId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTodayVisits_ShouldReturnVisits() {
        Visit visit = new Visit();
        VisitDto dto = new VisitDto();

        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(dto);

        List<VisitDto> result = visitService.getTodayVisits();

        assertThat(result).hasSize(1);
    }

    @Test
    void getClientVisitsCount_ShouldCountOnlyAttendedVisits() {
        Long clientId = 1L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        Visit attendedVisit = new Visit();
        Client client = new Client();
        client.setId(clientId);
        attendedVisit.setClient(client);
        attendedVisit.setStatus(VisitStatus.ATTENDED);

        Visit notAttendedVisit = new Visit();
        notAttendedVisit.setClient(client);
        notAttendedVisit.setStatus(VisitStatus.BOOKED);

        when(visitRepository.findByVisitTimeBetween(any(), any()))
                .thenReturn(List.of(attendedVisit, notAttendedVisit));

        long result = visitService.getClientVisitsCount(clientId, from, to);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void getClientHistory_ShouldFilterByClientId() {
        Long clientId = 1L;
        Long otherClientId = 2L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        Visit visitForClient = new Visit();
        Client client1 = new Client();
        client1.setId(clientId);
        visitForClient.setClient(client1);

        Visit visitForOtherClient = new Visit();
        Client client2 = new Client();
        client2.setId(otherClientId);
        visitForOtherClient.setClient(client2);

        when(visitRepository.findByVisitTimeBetween(any(), any()))
                .thenReturn(List.of(visitForClient, visitForOtherClient));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(new VisitDto());

        List<VisitDto> result = visitService.getClientHistory(clientId, from, to);

        assertThat(result).hasSize(1);
    }

    @Test
    void getClientVisitsCount_ShouldReturnZero_WhenNoAttendedVisits() {
        Long clientId = 1L;
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        Visit visit = new Visit();
        Client client = new Client();
        client.setId(clientId);
        visit.setClient(client);
        visit.setStatus(VisitStatus.BOOKED);

        when(visitRepository.findByVisitTimeBetween(any(), any())).thenReturn(List.of(visit));

        long result = visitService.getClientVisitsCount(clientId, from, to);

        assertThat(result).isZero();
    }


    @Test
    void markAttendance_ShouldSetNoShow_WhenAttendedIsFalse() {
        Long visitId = 1L;
        Visit visit = new Visit();
        visit.setId(visitId);
        visit.setStatus(VisitStatus.BOOKED);

        VisitDto responseDto = new VisitDto();

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);

        VisitDto result = visitService.markAttendance(visitId, false);

        assertThat(result).isNotNull();
        assertThat(visit.getStatus()).isEqualTo(VisitStatus.NO_SHOW);
        verify(visitRepository).save(visit);
    }

    @Test
    void bookWorkout_ShouldHandleSessionStatusConfirmed() {
        Long clientId = 1L;
        Long sessionId = 1L;
        Long subscriptionId = 1L;

        Client client = new Client();
        client.setId(clientId);

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        List<WorkoutType> allowed = new ArrayList<>();
        allowed.add(workoutType);

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        subscription.setAllowedWorkoutTypes(allowed);

        WorkoutSession session = new WorkoutSession();
        session.setId(sessionId);
        session.setStatus(WorkoutSessionStatus.CONFIRMED); // ← CONFIRMED, не SCHEDULED
        session.setWorkoutType(workoutType);
        session.setMaxParticipants(10);
        session.setDayOfWeek(DayOfWeek.MONDAY);
        session.setStartTime(LocalTime.of(10, 0));

        Visit visit = new Visit();
        visit.setId(1L);

        VisitDto responseDto = new VisitDto();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(visitRepository.findBookedVisitsByClient(clientId)).thenReturn(new ArrayList<>());
        when(visitRepository.findBookedVisitsBySession(sessionId)).thenReturn(new ArrayList<>());
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);

        VisitDto result = visitService.bookWorkout(clientId, sessionId, subscriptionId);

        assertThat(result).isNotNull();
        verify(visitRepository).save(any(Visit.class));
    }



    @Test
    void cancelBooking_ShouldThrowException_WhenVisitNotBooked() {
        Long visitId = 1L;
        Visit visit = new Visit();
        visit.setId(visitId);
        visit.setStatus(VisitStatus.ATTENDED); // Не BOOKED

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> visitService.cancelBooking(visitId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Неверный статус");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void findNextDateForDayOfWeek_ShouldHandleDaysUntilZeroOrPositive() {
        Long clientId = 1L;
        Long sessionId = 1L;
        Long subscriptionId = 1L;

        Client client = new Client();
        client.setId(clientId);

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        List<WorkoutType> allowed = new ArrayList<>();
        allowed.add(workoutType);

        Subscription subscription = new Subscription();
        subscription.setId(subscriptionId);
        subscription.setAllowedWorkoutTypes(allowed);

        // Тренировка в тот же день недели, что и сегодня
        WorkoutSession session = new WorkoutSession();
        session.setId(sessionId);
        session.setStatus(WorkoutSessionStatus.SCHEDULED);
        session.setWorkoutType(workoutType);
        session.setMaxParticipants(10);
        session.setDayOfWeek(LocalDate.now().getDayOfWeek()); // сегодняшний день
        session.setStartTime(LocalTime.of(10, 0));

        Visit visit = new Visit();
        visit.setId(1L);

        VisitDto responseDto = new VisitDto();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(visitRepository.findBookedVisitsByClient(clientId)).thenReturn(new ArrayList<>());
        when(visitRepository.findBookedVisitsBySession(sessionId)).thenReturn(new ArrayList<>());
        when(visitRepository.save(any(Visit.class))).thenReturn(visit);
        when(visitMapper.toDto(any(Visit.class))).thenReturn(responseDto);

        VisitDto result = visitService.bookWorkout(clientId, sessionId, subscriptionId);

        assertThat(result).isNotNull();
        verify(visitRepository).save(any(Visit.class));
    }

}
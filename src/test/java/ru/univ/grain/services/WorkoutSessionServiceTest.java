package ru.univ.grain.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.univ.grain.cache.SessionCache;
import ru.univ.grain.cache.SessionSearchKey;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.entities.*;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.WorkoutSessionMapper;
import ru.univ.grain.repositories.TrainerRepository;
import ru.univ.grain.repositories.VisitRepository;
import ru.univ.grain.repositories.WorkoutSessionRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;

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
class WorkoutSessionServiceTest {

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private WorkoutTypeRepository workoutTypeRepository;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private WorkoutSessionMapper workoutSessionMapper;

    @Mock
    private SessionCache sessionCache;

    @InjectMocks
    private WorkoutSessionService workoutSessionService;

    @BeforeEach
    void setUp() {
        workoutSessionService.init();
    }

    @Test
    void getAllSessions_ShouldReturnList() {
        WorkoutSession session1 = new WorkoutSession();
        WorkoutSession session2 = new WorkoutSession();
        WorkoutSessionDto dto1 = new WorkoutSessionDto();
        WorkoutSessionDto dto2 = new WorkoutSessionDto();

        when(workoutSessionRepository.findAll()).thenReturn(List.of(session1, session2));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto1, dto2);

        List<WorkoutSessionDto> result = workoutSessionService.getAllSessions();

        assertThat(result).hasSize(2);
    }

    @Test
    void getSessionById_ShouldReturnSession_WhenExists() {
        Long id = 1L;
        WorkoutSession session = new WorkoutSession();
        session.setId(id);
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findById(id)).thenReturn(Optional.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        WorkoutSessionDto result = workoutSessionService.getSessionById(id);

        assertThat(result).isNotNull();
    }

    @Test
    void getSessionById_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(workoutSessionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutSessionService.getSessionById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void createSession_ShouldReturnSession_WhenValid() {
        WorkoutSessionDto dto = new WorkoutSessionDto();
        dto.setTrainerId(1L);
        dto.setWorkoutTypeId(1L);
        dto.setDayOfWeek(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 30));
        dto.setMaxParticipants(10);
        dto.setStatus(WorkoutSessionStatus.SCHEDULED);

        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setLastName("Смирнова");

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        WorkoutSession session = new WorkoutSession();
        session.setId(1L);

        WorkoutSessionDto responseDto = new WorkoutSessionDto();

        when(workoutSessionRepository.findOverlappingSessions(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(workoutTypeRepository.findById(1L)).thenReturn(Optional.of(workoutType));
        when(workoutSessionMapper.toEntity(any(WorkoutSessionDto.class))).thenReturn(session);
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(responseDto);

        WorkoutSessionDto result = workoutSessionService.createSession(dto);

        assertThat(result).isNotNull();
        verify(sessionCache).clearByTrainerLastName("Смирнова");
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
    }

    @Test
    void createSession_ShouldThrowException_WhenTimeRangeInvalid() {
        WorkoutSessionDto dto = new WorkoutSessionDto();
        dto.setStartTime(LocalTime.of(12, 0));
        dto.setEndTime(LocalTime.of(11, 30));

        assertThatThrownBy(() -> workoutSessionService.createSession(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Время начала должно быть раньше");
    }

    @Test
    void createSession_ShouldThrowException_WhenOverlap() {
        WorkoutSessionDto dto = new WorkoutSessionDto();
        dto.setTrainerId(1L);
        dto.setWorkoutTypeId(1L);
        dto.setDayOfWeek(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 30));
        dto.setStatus(WorkoutSessionStatus.SCHEDULED);

        Trainer trainer = new Trainer();
        trainer.setId(1L);

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        WorkoutSession session = new WorkoutSession();
        session.setId(1L);

        when(workoutSessionRepository.findOverlappingSessions(any(), any(), any(), any()))
                .thenReturn(List.of(new WorkoutSession()));

        assertThatThrownBy(() -> workoutSessionService.createSession(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже есть тренировка");
    }

    @Test
    void createSession_ShouldThrowException_WhenTrainerNotFound() {
        WorkoutSessionDto dto = new WorkoutSessionDto();
        dto.setTrainerId(1L);
        dto.setWorkoutTypeId(1L);
        dto.setDayOfWeek(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 30));
        dto.setStatus(WorkoutSessionStatus.SCHEDULED);

        when(workoutSessionRepository.findOverlappingSessions(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(trainerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutSessionService.createSession(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void updateSession_ShouldUpdateSession_WhenValid() {
        Long id = 1L;
        WorkoutSessionDto dto = new WorkoutSessionDto();
        dto.setTrainerId(1L);
        dto.setWorkoutTypeId(1L);
        dto.setDayOfWeek(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 30));
        dto.setStatus(WorkoutSessionStatus.SCHEDULED);

        Trainer oldTrainer = new Trainer();
        oldTrainer.setId(1L);
        oldTrainer.setLastName("Смирнова");

        Trainer newTrainer = new Trainer();
        newTrainer.setId(1L);
        newTrainer.setLastName("Смирнова");

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        WorkoutSession existing = new WorkoutSession();
        existing.setId(id);
        existing.setTrainer(oldTrainer);

        WorkoutSession updated = new WorkoutSession();
        updated.setId(id);

        WorkoutSessionDto responseDto = new WorkoutSessionDto();

        when(workoutSessionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(workoutSessionRepository.findOverlappingSessions(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(newTrainer));
        when(workoutTypeRepository.findById(1L)).thenReturn(Optional.of(workoutType));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(updated);
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(responseDto);

        WorkoutSessionDto result = workoutSessionService.updateSession(id, dto);

        assertThat(result).isNotNull();
        verify(workoutSessionMapper).updateEntity(dto, existing);
        verify(sessionCache).clearByTrainerLastName("Смирнова");
    }

    @Test
    void updateSession_ShouldThrowException_WhenNotFound() {
        Long id = 999L;
        WorkoutSessionDto dto = new WorkoutSessionDto();
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 30));

        when(workoutSessionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutSessionService.updateSession(id, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void deleteSession_ShouldDeleteSession_WhenNoFutureVisits() {
        Long id = 1L;
        WorkoutSession session = new WorkoutSession();
        session.setId(id);
        Trainer trainer = new Trainer();
        trainer.setLastName("Смирнова");
        session.setTrainer(trainer);

        when(workoutSessionRepository.findById(id)).thenReturn(Optional.of(session));
        when(visitRepository.findByWorkoutSessionId(id)).thenReturn(new ArrayList<>());

        workoutSessionService.deleteSession(id);

        verify(workoutSessionRepository).delete(session);
        verify(sessionCache).clearByTrainerLastName("Смирнова");
    }

    @Test
    void deleteSession_ShouldThrowException_WhenFutureVisitsExist() {
        Long id = 1L;
        WorkoutSession session = new WorkoutSession();
        session.setId(id);

        Visit futureVisit = new Visit();
        futureVisit.setVisitTime(LocalDateTime.now().plusDays(1));
        futureVisit.setStatus(VisitStatus.BOOKED);

        when(workoutSessionRepository.findById(id)).thenReturn(Optional.of(session));
        when(visitRepository.findByWorkoutSessionId(id)).thenReturn(List.of(futureVisit));

        assertThatThrownBy(() -> workoutSessionService.deleteSession(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("будущие записи");

        verify(workoutSessionRepository, never()).delete(any());
    }

    @Test
    void getSessionsByTrainer_ShouldReturnList() {
        Long trainerId = 1L;
        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByTrainerId(trainerId)).thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.getSessionsByTrainer(trainerId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getSessionsByDay_ShouldReturnList() {
        DayOfWeek day = DayOfWeek.MONDAY;
        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByDayOfWeek(day)).thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.getSessionsByDay(day);

        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveSessionsByDay_ShouldReturnList() {
        DayOfWeek day = DayOfWeek.MONDAY;
        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByDayOfWeekAndStatus(day, WorkoutSessionStatus.SCHEDULED))
                .thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.getActiveSessionsByDay(day);

        assertThat(result).hasSize(1);
    }

    @Test
    void getSessionsByWorkoutType_ShouldReturnList() {
        Long workoutTypeId = 1L;
        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByWorkoutTypeId(workoutTypeId)).thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.getSessionsByWorkoutType(workoutTypeId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTodaySessions_ShouldReturnList() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByDayOfWeekAndStatus(today, WorkoutSessionStatus.SCHEDULED))
                .thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.getTodaySessions();

        assertThat(result).hasSize(1);
    }

    @Test
    void findOverlappingSessions_ShouldReturnList() {
        Long trainerId = 1L;
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findOverlappingSessions(trainerId, day, start, end))
                .thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.findOverlappingSessions(trainerId, day, start, end);

        assertThat(result).hasSize(1);
    }

    @Test
    void isTrainerAvailable_ShouldReturnTrue_WhenNoOverlap() {
        Long trainerId = 1L;
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        when(workoutSessionRepository.findOverlappingSessions(trainerId, day, start, end))
                .thenReturn(new ArrayList<>());

        boolean result = workoutSessionService.isTrainerAvailable(trainerId, day, start, end);

        assertThat(result).isTrue();
    }

    @Test
    void isTrainerAvailable_ShouldReturnFalse_WhenOverlapExists() {
        Long trainerId = 1L;
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        when(workoutSessionRepository.findOverlappingSessions(trainerId, day, start, end))
                .thenReturn(List.of(new WorkoutSession()));

        boolean result = workoutSessionService.isTrainerAvailable(trainerId, day, start, end);

        assertThat(result).isFalse();
    }

    @Test
    void getBookedCount_ShouldReturnCount() {
        Long sessionId = 1L;
        Visit visit = new Visit();
        visit.setVisitTime(LocalDateTime.now().plusDays(1));
        visit.setStatus(VisitStatus.BOOKED);

        when(visitRepository.findByWorkoutSessionId(sessionId)).thenReturn(List.of(visit, visit));

        long result = workoutSessionService.getBookedCount(sessionId);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void hasAvailableSpots_ShouldReturnTrue_WhenSpotsAvailable() {
        Long sessionId = 1L;
        WorkoutSession session = new WorkoutSession();
        session.setMaxParticipants(10);

        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(visitRepository.findByWorkoutSessionId(sessionId)).thenReturn(new ArrayList<>());

        boolean result = workoutSessionService.hasAvailableSpots(sessionId);

        assertThat(result).isTrue();
    }

    @Test
    void hasAvailableSpots_ShouldReturnFalse_WhenFullyBooked() {
        Long sessionId = 1L;
        WorkoutSession session = new WorkoutSession();
        session.setMaxParticipants(5);

        List<Visit> visits = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Visit visit = new Visit();
            visit.setVisitTime(LocalDateTime.now().plusDays(1));
            visit.setStatus(VisitStatus.BOOKED);
            visits.add(visit);
        }

        when(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(visitRepository.findByWorkoutSessionId(sessionId)).thenReturn(visits);

        boolean result = workoutSessionService.hasAvailableSpots(sessionId);

        assertThat(result).isFalse();
    }

    @Test
    void getSessionsByStatus_ShouldReturnList() {
        WorkoutSessionStatus status = WorkoutSessionStatus.SCHEDULED;
        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByStatus(status)).thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.getSessionsByStatus(status);

        assertThat(result).hasSize(1);
    }

    @Test
    void getSessionsByTime_ShouldReturnList() {
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime time = LocalTime.of(10, 0);
        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByTime(day, time)).thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.getSessionsByTime(day, time);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllScheduledSessions_ShouldReturnList() {
        WorkoutSession session = new WorkoutSession();
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findAllScheduled()).thenReturn(List.of(session));
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        List<WorkoutSessionDto> result = workoutSessionService.getAllScheduledSessions();

        assertThat(result).hasSize(1);
    }

    @Test
    void updateSessionStatus_ShouldUpdateStatus_WhenValid() {
        Long id = 1L;
        WorkoutSessionStatus newStatus = WorkoutSessionStatus.CONFIRMED;
        WorkoutSession session = new WorkoutSession();
        session.setId(id);
        Trainer trainer = new Trainer();
        trainer.setLastName("Смирнова");
        session.setTrainer(trainer);

        WorkoutSessionDto responseDto = new WorkoutSessionDto();

        when(workoutSessionRepository.findById(id)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session);
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(responseDto);

        WorkoutSessionDto result = workoutSessionService.updateSessionStatus(id, newStatus);

        assertThat(result).isNotNull();
        assertThat(session.getStatus()).isEqualTo(newStatus);
        verify(sessionCache).clearByTrainerLastName("Смирнова");
    }

    @Test
    void updateSessionStatus_ShouldCancelFutureVisits_WhenCancelled() {
        Long id = 1L;
        WorkoutSessionStatus newStatus = WorkoutSessionStatus.CANCELLED;
        WorkoutSession session = new WorkoutSession();
        session.setId(id);
        session.setStatus(WorkoutSessionStatus.SCHEDULED);
        Trainer trainer = new Trainer();
        trainer.setLastName("Смирнова");
        session.setTrainer(trainer);

        Visit futureVisit = new Visit();
        futureVisit.setVisitTime(LocalDateTime.now().plusDays(1));
        futureVisit.setStatus(VisitStatus.BOOKED);

        when(workoutSessionRepository.findById(id)).thenReturn(Optional.of(session));
        when(visitRepository.findByWorkoutSessionId(id)).thenReturn(List.of(futureVisit));

        workoutSessionService.updateSessionStatus(id, newStatus);

        assertThat(futureVisit.getStatus()).isEqualTo(VisitStatus.CANCELLED);
        verify(visitRepository).findByWorkoutSessionId(id);
    }

    @Test
    void getSessionsByTrainerLastNameAndDay_ShouldReturnPage() {
        String lastName = "Смирнова";
        DayOfWeek day = DayOfWeek.MONDAY;
        int page = 0;
        int size = 10;

        WorkoutSession session = new WorkoutSession();
        Page<WorkoutSession> sessionPage = new PageImpl<>(List.of(session));
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByTrainerLastNameAndDay(eq(lastName), eq(day), any()))
                .thenReturn(sessionPage);
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        Page<WorkoutSessionDto> result = workoutSessionService.getSessionsByTrainerLastNameAndDay(lastName, day, page, size);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getSessionsByTrainerLastNameAndDayCached_ShouldReturnFromCache_WhenHit() {
        String lastName = "Смирнова";
        DayOfWeek day = DayOfWeek.MONDAY;
        int page = 0;
        int size = 10;

        Page<WorkoutSessionDto> cachedPage = new PageImpl<>(new ArrayList<>());
        SessionSearchKey key = new SessionSearchKey(lastName, day, page, size, "startTime");

        when(sessionCache.get(key)).thenReturn(cachedPage);

        Page<WorkoutSessionDto> result = workoutSessionService.getSessionsByTrainerLastNameAndDayCached(lastName, day, page, size);

        assertThat(result).isEqualTo(cachedPage);
        verify(workoutSessionRepository, never()).findByTrainerLastNameAndDay(any(), any(), any());
    }

    @Test
    void getSessionsByTrainerLastNameAndDayNative_ShouldReturnPage() {
        String lastName = "Смирнова";
        DayOfWeek day = DayOfWeek.MONDAY;
        int page = 0;
        int size = 10;

        WorkoutSession session = new WorkoutSession();
        Page<WorkoutSession> sessionPage = new PageImpl<>(List.of(session));
        WorkoutSessionDto dto = new WorkoutSessionDto();

        when(workoutSessionRepository.findByTrainerLastNameAndDayNative(eq(lastName), eq("MONDAY"), any()))
                .thenReturn(sessionPage);
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(dto);

        Page<WorkoutSessionDto> result = workoutSessionService.getSessionsByTrainerLastNameAndDayNative(lastName, day, page, size);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void createSessionsBulkWithTransaction_ShouldCreateAll_WhenValid() {
        WorkoutSessionDto dto1 = new WorkoutSessionDto();
        dto1.setTrainerId(1L);
        dto1.setWorkoutTypeId(1L);
        dto1.setDayOfWeek(DayOfWeek.MONDAY);
        dto1.setStartTime(LocalTime.of(10, 0));
        dto1.setEndTime(LocalTime.of(11, 30));
        dto1.setMaxParticipants(10);

        WorkoutSessionDto dto2 = new WorkoutSessionDto();
        dto2.setTrainerId(1L);
        dto2.setWorkoutTypeId(1L);
        dto2.setDayOfWeek(DayOfWeek.TUESDAY);
        dto2.setStartTime(LocalTime.of(14, 0));
        dto2.setEndTime(LocalTime.of(15, 30));
        dto2.setMaxParticipants(10);

        List<WorkoutSessionDto> dtos = List.of(dto1, dto2);

        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setLastName("Смирнова");

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        WorkoutSession session1 = new WorkoutSession();
        session1.setId(1L);
        WorkoutSession session2 = new WorkoutSession();
        session2.setId(2L);

        WorkoutSessionDto response1 = new WorkoutSessionDto();
        WorkoutSessionDto response2 = new WorkoutSessionDto();

        when(workoutSessionRepository.findOverlappingSessionsForTrainer(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(workoutTypeRepository.findById(1L)).thenReturn(Optional.of(workoutType));
        when(workoutSessionMapper.toEntity(any(WorkoutSessionDto.class)))
                .thenReturn(session1, session2);
        when(workoutSessionRepository.save(any(WorkoutSession.class)))
                .thenReturn(session1, session2);
        when(workoutSessionMapper.toDto(any(WorkoutSession.class)))
                .thenReturn(response1, response2);

        List<WorkoutSessionDto> result = workoutSessionService.createSessionsBulkWithTransaction(dtos);

        assertThat(result).hasSize(2);
        verify(workoutSessionRepository, times(2)).save(any(WorkoutSession.class));
    }

    @Test
    void createSessionsBulkWithTransaction_ShouldThrowException_WhenError() {
        WorkoutSessionDto dto1 = new WorkoutSessionDto();
        dto1.setTrainerId(1L);
        dto1.setWorkoutTypeId(1L);
        dto1.setDayOfWeek(DayOfWeek.MONDAY);
        dto1.setStartTime(LocalTime.of(10, 0));
        dto1.setEndTime(LocalTime.of(11, 30));

        WorkoutSessionDto dto2 = new WorkoutSessionDto();
        dto2.setTrainerId(1L);
        dto2.setWorkoutTypeId(1L);
        dto2.setDayOfWeek(DayOfWeek.MONDAY);
        dto2.setStartTime(LocalTime.of(10, 0));
        dto2.setEndTime(LocalTime.of(11, 30));

        List<WorkoutSessionDto> dtos = List.of(dto1, dto2);

        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setLastName("Смирнова");

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        WorkoutSession session = new WorkoutSession();

        when(workoutSessionRepository.findOverlappingSessionsForTrainer(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>())
                .thenReturn(List.of(new WorkoutSession()));
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(workoutTypeRepository.findById(1L)).thenReturn(Optional.of(workoutType));
        when(workoutSessionMapper.toEntity(any(WorkoutSessionDto.class)))
                .thenReturn(session, new WorkoutSession());

        assertThatThrownBy(() -> workoutSessionService.createSessionsBulkWithTransaction(dtos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже есть тренировка");
    }

    @Test
    void createSessionsBulkWithoutTransaction_ShouldCreatePartially_WhenError() {
        WorkoutSessionDto dto1 = new WorkoutSessionDto();
        dto1.setTrainerId(1L);
        dto1.setWorkoutTypeId(1L);
        dto1.setDayOfWeek(DayOfWeek.MONDAY);
        dto1.setStartTime(LocalTime.of(10, 0));
        dto1.setEndTime(LocalTime.of(11, 30));
        dto1.setMaxParticipants(10);
        dto1.setStatus(WorkoutSessionStatus.SCHEDULED);

        WorkoutSessionDto dto2 = new WorkoutSessionDto();
        dto2.setTrainerId(1L);
        dto2.setWorkoutTypeId(1L);
        dto2.setDayOfWeek(DayOfWeek.MONDAY);
        dto2.setStartTime(LocalTime.of(10, 0));
        dto2.setEndTime(LocalTime.of(11, 30));
        dto2.setMaxParticipants(10);
        dto2.setStatus(WorkoutSessionStatus.SCHEDULED);

        List<WorkoutSessionDto> dtos = List.of(dto1, dto2);

        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setLastName("Смирнова");

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);

        WorkoutSession session1 = new WorkoutSession();
        session1.setId(1L);
        WorkoutSessionDto response1 = new WorkoutSessionDto();

        when(workoutSessionRepository.findOverlappingSessionsForTrainer(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>())
                .thenReturn(List.of(new WorkoutSession()));
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(workoutTypeRepository.findById(1L)).thenReturn(Optional.of(workoutType));
        when(workoutSessionMapper.toEntity(any(WorkoutSessionDto.class))).thenReturn(session1);
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(session1);
        when(workoutSessionMapper.toDto(any(WorkoutSession.class))).thenReturn(response1);

        assertThatThrownBy(() -> workoutSessionService.createSessionsBulkWithoutTransaction(dtos))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже есть тренировка");

        verify(workoutSessionRepository, times(1)).save(any(WorkoutSession.class));
    }
}
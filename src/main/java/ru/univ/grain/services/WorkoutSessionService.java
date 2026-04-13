package ru.univ.grain.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.cache.AppCache;
import ru.univ.grain.cache.CacheKey;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final TrainerRepository trainerRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final VisitRepository visitRepository;
    private final WorkoutSessionMapper workoutSessionMapper;
    private final AppCache appCache;

    private static final String SESSION_NOT_FOUND = "Тренировка с id %d не найдена";
    private static final String TRAINER_NOT_FOUND = "Тренер с id %d не найден";
    private static final String WORKOUT_TYPE_NOT_FOUND = "Тип тренировки с id %d не найден";
    private static final String SESSION_OVERLAP = "У тренера уже есть тренировка в это время";
    private static final String FUTURE_VISITS_EXIST = "Невозможно удалить тренировку: есть будущие записи клиентов";
    private static final String INVALID_TIME_RANGE = "Время начала должно быть раньше времени окончания";

    private WorkoutSessionService self;

    @PostConstruct
    public void init() {
        this.self = this;
    }

    private void validateTimeRange(final LocalTime startTime, final LocalTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new BusinessException(INVALID_TIME_RANGE);
        }
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getAllSessions() {
        return workoutSessionRepository.findAll().stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutSessionDto getSessionById(final Long id) {
        final WorkoutSession session = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SESSION_NOT_FOUND, id)));
        return workoutSessionMapper.toDto(session);
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getSessionsByTrainer(final Long trainerId) {
        return workoutSessionRepository.findByTrainerId(trainerId).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getSessionsByDay(final DayOfWeek dayOfWeek) {
        return workoutSessionRepository.findByDayOfWeek(dayOfWeek).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getActiveSessionsByDay(final DayOfWeek dayOfWeek) {
        return workoutSessionRepository.findByDayOfWeekAndStatus(dayOfWeek, WorkoutSessionStatus.SCHEDULED).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getSessionsByWorkoutType(final Long workoutTypeId) {
        return workoutSessionRepository.findByWorkoutTypeId(workoutTypeId).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getSessionsByStatus(final WorkoutSessionStatus status) {
        return workoutSessionRepository.findByStatus(status).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getSessionsByTime(final DayOfWeek dayOfWeek, final LocalTime time) {
        return workoutSessionRepository.findByTime(dayOfWeek, time).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getTodaySessions() {
        final DayOfWeek today = LocalDate.now().getDayOfWeek();
        return workoutSessionRepository.findByDayOfWeekAndStatus(today, WorkoutSessionStatus.SCHEDULED).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getSessionsForDate(final LocalDate date) {
        final CacheKey key = CacheKey.forSessionsDate(date);

        final List<WorkoutSessionDto> cached = appCache.get(key);
        if (cached != null) {
            return cached;
        }

        final DayOfWeek dayOfWeek = date.getDayOfWeek();
        final List<WorkoutSessionDto> result = workoutSessionRepository.findSessionsForDate(date, dayOfWeek).stream()
                .map(workoutSessionMapper::toDto)
                .toList();

        appCache.put(key, result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getSessionsByDateRange(final LocalDate startDate, final LocalDate endDate) {
        return workoutSessionRepository.findBySessionDateBetween(startDate, endDate).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> findOverlappingSessions(
            final Long trainerId,
            final DayOfWeek dayOfWeek,
            final LocalTime start,
            final LocalTime end) {
        return workoutSessionRepository.findOverlappingSessions(trainerId, dayOfWeek, start, end).stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isTrainerAvailable(
            final Long trainerId,
            final DayOfWeek dayOfWeek,
            final LocalTime start,
            final LocalTime end) {
        validateTimeRange(start, end);
        return workoutSessionRepository.findOverlappingSessions(trainerId, dayOfWeek, start, end).isEmpty();
    }

    @Transactional(readOnly = true)
    public long getBookedCount(final Long sessionId) {
        return visitRepository.countByWorkoutSessionIdAndStatus(sessionId, VisitStatus.BOOKED);
    }

    @Transactional(readOnly = true)
    public boolean hasAvailableSpots(final Long sessionId) {
        final long bookedCount = self.getBookedCount(sessionId);
        final WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SESSION_NOT_FOUND, sessionId)));
        return bookedCount < session.getMaxParticipants();
    }

    @Transactional
    public WorkoutSessionDto createSession(final WorkoutSessionDto dto) {
        validateTimeRange(dto.getStartTime(), dto.getEndTime());

        final List<WorkoutSession> overlapping = workoutSessionRepository.findOverlappingSessionsForTrainer(
                dto.getTrainerId(),
                dto.getDayOfWeek(),
                dto.getStartTime(),
                dto.getEndTime()
        );

        if (!overlapping.isEmpty()) {
            throw new BusinessException(SESSION_OVERLAP);
        }

        final Trainer trainer = trainerRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, dto.getTrainerId())));

        final WorkoutType workoutType = workoutTypeRepository.findById(dto.getWorkoutTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(WORKOUT_TYPE_NOT_FOUND, dto.getWorkoutTypeId())));

        final WorkoutSession session = workoutSessionMapper.toEntity(dto);
        session.setTrainer(trainer);
        session.setWorkoutType(workoutType);

        final WorkoutSession saved = workoutSessionRepository.save(session);

        evictSessionCache(saved);
        return workoutSessionMapper.toDto(saved);
    }

    @Transactional
    public WorkoutSessionDto updateSession(final Long id, final WorkoutSessionDto dto) {
        validateTimeRange(dto.getStartTime(), dto.getEndTime());

        final WorkoutSession existing = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SESSION_NOT_FOUND, id)));

        final List<WorkoutSession> overlapping = workoutSessionRepository.findOverlappingSessionsForTrainer(
                        dto.getTrainerId(),
                        dto.getDayOfWeek(),
                        dto.getStartTime(),
                        dto.getEndTime()
                ).stream()
                .filter(s -> !s.getId().equals(id))
                .toList();

        if (!overlapping.isEmpty()) {
            throw new BusinessException(SESSION_OVERLAP);
        }

        final Trainer trainer = trainerRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, dto.getTrainerId())));

        final WorkoutType workoutType = workoutTypeRepository.findById(dto.getWorkoutTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(WORKOUT_TYPE_NOT_FOUND, dto.getWorkoutTypeId())));

        workoutSessionMapper.updateEntity(dto, existing);
        existing.setTrainer(trainer);
        existing.setWorkoutType(workoutType);

        final WorkoutSession updated = workoutSessionRepository.save(existing);

        evictSessionCache(updated);
        if (dto.getSessionDate() != null) {
            appCache.evictSessionsForDate(dto.getSessionDate());
        }
        return workoutSessionMapper.toDto(updated);
    }

    @Transactional
    public WorkoutSessionDto updateSessionStatus(final Long id, final WorkoutSessionStatus status) {
        final WorkoutSession session = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SESSION_NOT_FOUND, id)));

        if (status == WorkoutSessionStatus.CANCELLED &&
                session.getStatus() != WorkoutSessionStatus.COMPLETED) {
            final LocalDateTime now = LocalDateTime.now();
            final List<Visit> futureVisits = visitRepository.findByWorkoutSessionId(id).stream()
                    .filter(v -> v.getVisitTime().isAfter(now) && v.getStatus() == VisitStatus.BOOKED)
                    .toList();
            futureVisits.forEach(v -> v.setStatus(VisitStatus.CANCELLED));
        }

        session.setStatus(status);
        final WorkoutSession updated = workoutSessionRepository.save(session);

        evictSessionCache(updated);
        return workoutSessionMapper.toDto(updated);
    }

    @Transactional
    public void deleteSession(final Long id) {
        final WorkoutSession session = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SESSION_NOT_FOUND, id)));

        final LocalDateTime now = LocalDateTime.now();
        final List<Visit> futureVisits = visitRepository.findByWorkoutSessionId(id).stream()
                .filter(v -> v.getVisitTime().isAfter(now) && v.getStatus() == VisitStatus.BOOKED)
                .toList();

        if (!futureVisits.isEmpty()) {
            throw new BusinessException(FUTURE_VISITS_EXIST);
        }

        evictSessionCache(session);
        workoutSessionRepository.delete(session);
    }

    private void evictSessionCache(final WorkoutSession session) {
        if (session.getSessionDate() != null) {
            appCache.evictSessionsForDate(session.getSessionDate());
        }
        if (session.getTrainer() != null) {
            appCache.evictSessionsForTrainer(session.getTrainer().getId());
        }
    }
}

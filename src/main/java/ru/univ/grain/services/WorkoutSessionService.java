package ru.univ.grain.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.cache.SessionCache;
import ru.univ.grain.cache.SessionSearchKey;
import ru.univ.grain.entities.*;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.mapper.WorkoutSessionMapper;
import ru.univ.grain.repositories.WorkoutSessionRepository;
import ru.univ.grain.repositories.TrainerRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;
import ru.univ.grain.repositories.VisitRepository;
import ru.univ.grain.exception.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final TrainerRepository trainerRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final VisitRepository visitRepository;
    private final WorkoutSessionMapper workoutSessionMapper;
    private final SessionCache sessionCache;

    private WorkoutSessionService self;

    @PostConstruct
    public void init() {
        this.self = this;
    }

    private static final String SESSION_NOT_FOUND = "Тренировка с id %d не найдена";
    private static final String TRAINER_NOT_FOUND = "Тренер с id %d не найден";
    private static final String WORKOUT_TYPE_NOT_FOUND = "Тип тренировки с id %d не найден";
    private static final String SESSION_OVERLAP = "У тренера уже есть тренировка в это время";
    private static final String FUTURE_VISITS_EXIST = "Невозможно удалить тренировку: есть будущие записи клиентов";
    private static final String INVALID_TIME_RANGE = "Время начала должно быть раньше времени окончания";

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
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

    @Transactional
    public WorkoutSessionDto createSession(final WorkoutSessionDto dto) {
        validateTimeRange(dto.getStartTime(), dto.getEndTime());

        final List<WorkoutSession> overlapping = findOverlappingSessionsInternal(
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

        sessionCache.clearByTrainerLastName(trainer.getLastName());

        return workoutSessionMapper.toDto(saved);
    }

    @Transactional
    public WorkoutSessionDto updateSession(final Long id, final WorkoutSessionDto dto) {
        validateTimeRange(dto.getStartTime(), dto.getEndTime());

        final WorkoutSession existing = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SESSION_NOT_FOUND, id)));

        final List<WorkoutSession> overlapping = findOverlappingSessionsInternal(
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

        final String oldTrainerLastName = existing.getTrainer().getLastName();
        final String newTrainerLastName = trainer.getLastName();

        workoutSessionMapper.updateEntity(dto, existing);
        existing.setTrainer(trainer);
        existing.setWorkoutType(workoutType);

        final WorkoutSession updated = workoutSessionRepository.save(existing);

        sessionCache.clearByTrainerLastName(oldTrainerLastName);
        if (!oldTrainerLastName.equals(newTrainerLastName)) {
            sessionCache.clearByTrainerLastName(newTrainerLastName);
        }

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

        final String trainerLastName = session.getTrainer().getLastName();
        workoutSessionRepository.delete(session);

        sessionCache.clearByTrainerLastName(trainerLastName);
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
    public List<WorkoutSessionDto> getTodaySessions() {
        final DayOfWeek today = LocalDate.now().getDayOfWeek();
        return workoutSessionRepository.findByDayOfWeekAndStatus(today, WorkoutSessionStatus.SCHEDULED).stream()
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

    private List<WorkoutSession> findOverlappingSessionsInternal(
            final Long trainerId,
            final DayOfWeek dayOfWeek,
            final LocalTime start,
            final LocalTime end) {
        return workoutSessionRepository.findOverlappingSessions(trainerId, dayOfWeek, start, end);
    }

    @Transactional(readOnly = true)
    public boolean isTrainerAvailable(
            final Long trainerId,
            final DayOfWeek dayOfWeek,
            final LocalTime start,
            final LocalTime end) {
        validateTimeRange(start, end);
        return findOverlappingSessionsInternal(trainerId, dayOfWeek, start, end).isEmpty();
    }

    @Transactional(readOnly = true)
    public long getBookedCount(final Long sessionId) {
        final LocalDateTime now = LocalDateTime.now();
        return visitRepository.findByWorkoutSessionId(sessionId).stream()
                .filter(v -> v.getVisitTime().isAfter(now) && v.getStatus() == VisitStatus.BOOKED)
                .count();
    }

    @Transactional(readOnly = true)
    public boolean hasAvailableSpots(final Long sessionId) {
        final long bookedCount = self.getBookedCount(sessionId);
        final WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SESSION_NOT_FOUND, sessionId)));
        return bookedCount < session.getMaxParticipants();
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
    public List<WorkoutSessionDto> getAllScheduledSessions() {
        return workoutSessionRepository.findAllScheduled().stream()
                .map(workoutSessionMapper::toDto)
                .toList();
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

        sessionCache.clearByTrainerLastName(session.getTrainer().getLastName());

        return workoutSessionMapper.toDto(updated);
    }

    private Pageable createPageable(int page, int size, String sortField) {
        return PageRequest.of(page, size, Sort.by(sortField).ascending());
    }

    @Transactional(readOnly = true)
    public Page<WorkoutSessionDto> getSessionsByTrainerLastNameAndDay(
            String trainerLastName,
            DayOfWeek dayOfWeek,
            int page,
            int size
    ) {
        final Pageable pageable = createPageable(page, size, "startTime");

        return workoutSessionRepository.findByTrainerLastNameAndDay(
                trainerLastName,
                dayOfWeek,
                pageable
        ).map(workoutSessionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<WorkoutSessionDto> getSessionsByTrainerLastNameAndDayCached(
            String trainerLastName,
            DayOfWeek dayOfWeek,
            int page,
            int size
    ) {
        final SessionSearchKey key = new SessionSearchKey(trainerLastName, dayOfWeek, page, size, "startTime");

        final Page<WorkoutSessionDto> cached = sessionCache.get(key);
        if (cached != null) {
            return cached;
        }

        final Page<WorkoutSessionDto> result = self.getSessionsByTrainerLastNameAndDay(
                trainerLastName, dayOfWeek, page, size
        );

        sessionCache.put(key, result);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<WorkoutSessionDto> getSessionsByTrainerLastNameAndDayNative(
            String trainerLastName,
            DayOfWeek dayOfWeek,
            int page,
            int size
    ) {
        final Pageable pageable = createPageable(page, size, "start_time");
        final String dayOfWeekStr = dayOfWeek.name();

        return workoutSessionRepository.findByTrainerLastNameAndDayNative(
                trainerLastName,
                dayOfWeekStr,
                pageable
        ).map(workoutSessionMapper::toDto);
    }



    @Transactional
    public List<WorkoutSessionDto> createSessionsBulkWithTransaction(List<WorkoutSessionDto> dtos) {
        return dtos.stream()
                .map(this::validateAndCreateSession)
                .toList();
    }

    public List<WorkoutSessionDto> createSessionsBulkWithoutTransaction(List<WorkoutSessionDto> dtos) {
        final List<WorkoutSessionDto> created = new ArrayList<>();

        for (WorkoutSessionDto dto : dtos) {
            created.add(validateAndCreateSession(dto));
        }

        return created;
    }

    private WorkoutSessionDto validateAndCreateSession(WorkoutSessionDto dto) {

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new BusinessException(INVALID_TIME_RANGE);
        }

        final List<WorkoutSession> overlapping = workoutSessionRepository
                .findOverlappingSessionsForTrainer(
                        dto.getTrainerId(),
                        dto.getDayOfWeek(),
                        dto.getStartTime(),
                        dto.getEndTime()
                );

        if (!overlapping.isEmpty()) {
            throw new BusinessException(
                    String.format(SESSION_OVERLAP, dto.getTrainerId())
            );
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


        sessionCache.clearByTrainerLastName(trainer.getLastName());

        return workoutSessionMapper.toDto(saved);
    }
}

package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.entities.*;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.mapper.WorkoutSessionMapper;
import ru.univ.grain.repositories.WorkoutSessionRepository;
import ru.univ.grain.repositories.TrainerRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;
import ru.univ.grain.repositories.VisitRepository;

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

    @Transactional(readOnly = true)
    public List<WorkoutSessionDto> getAllSessions() {
        return workoutSessionRepository.findAll().stream()
                .map(workoutSessionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutSessionDto getSessionById(final Long id) {
        return workoutSessionRepository.findById(id)
                .map(workoutSessionMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public WorkoutSessionDto createSession(final WorkoutSessionDto dto) {
        final List<WorkoutSession> overlapping = findOverlappingSessionsInternal(
                dto.getTrainerId(),
                dto.getDayOfWeek(),
                dto.getStartTime(),
                dto.getEndTime()
        );

        if (!overlapping.isEmpty()) {
            return null;
        }

        final Trainer trainer = trainerRepository.findById(dto.getTrainerId()).orElse(null);
        final WorkoutType workoutType = workoutTypeRepository.findById(dto.getWorkoutTypeId()).orElse(null);

        if (trainer == null || workoutType == null) {
            return null;
        }

        final WorkoutSession session = workoutSessionMapper.toEntity(dto);
        session.setTrainer(trainer);
        session.setWorkoutType(workoutType);

        final WorkoutSession saved = workoutSessionRepository.save(session);
        return workoutSessionMapper.toDto(saved);
    }

    @Transactional
    public WorkoutSessionDto updateSession(final Long id, final WorkoutSessionDto dto) {
        final WorkoutSession existing = workoutSessionRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        final List<WorkoutSession> overlapping = findOverlappingSessionsInternal(
                dto.getTrainerId(),
                dto.getDayOfWeek(),
                dto.getStartTime(),
                dto.getEndTime()
        ).stream()
                .filter(s -> !s.getId().equals(id))
                .toList();

        if (!overlapping.isEmpty()) {
            return null;
        }

        final Trainer trainer = trainerRepository.findById(dto.getTrainerId()).orElse(null);
        final WorkoutType workoutType = workoutTypeRepository.findById(dto.getWorkoutTypeId()).orElse(null);

        if (trainer == null || workoutType == null) {
            return null;
        }

        workoutSessionMapper.updateEntity(dto, existing);
        existing.setTrainer(trainer);
        existing.setWorkoutType(workoutType);

        final WorkoutSession updated = workoutSessionRepository.save(existing);
        return workoutSessionMapper.toDto(updated);
    }

    @Transactional
    public boolean deleteSession(final Long id) {
        final WorkoutSession session = workoutSessionRepository.findById(id).orElse(null);
        if (session == null) {
            return false;
        }

        final LocalDateTime now = LocalDateTime.now();
        final List<Visit> futureVisits = visitRepository.findByWorkoutSessionId(id).stream()
                .filter(v -> v.getVisitTime().isAfter(now) && v.getStatus() == VisitStatus.BOOKED)
                .toList();

        if (!futureVisits.isEmpty()) {
            return false;
        }

        workoutSessionRepository.deleteById(id);
        return true;
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
        return findOverlappingSessionsInternal(trainerId, dayOfWeek, start, end).isEmpty();
    }

    private long getBookedCountInternal(final Long sessionId) {
        final WorkoutSession session = workoutSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return 0;
        }

        final LocalDateTime now = LocalDateTime.now();
        return visitRepository.findByWorkoutSessionId(sessionId).stream()
                .filter(v -> v.getVisitTime().isAfter(now) && v.getStatus() == VisitStatus.BOOKED)
                .count();
    }

    @Transactional(readOnly = true)
    public long getBookedCount(final Long sessionId) {
        return getBookedCountInternal(sessionId);
    }

    @Transactional(readOnly = true)
    public boolean hasAvailableSpots(final Long sessionId) {
        final long bookedCount = getBookedCountInternal(sessionId);
        final WorkoutSession session = workoutSessionRepository.findById(sessionId).orElse(null);
        return session != null && bookedCount < session.getMaxParticipants();
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
        final WorkoutSession session = workoutSessionRepository.findById(id).orElse(null);
        if (session == null) {
            return null;
        }


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
        return workoutSessionMapper.toDto(updated);
    }

}

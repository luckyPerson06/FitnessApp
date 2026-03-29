package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.entities.*;
import ru.univ.grain.repositories.ClientRepository;
import ru.univ.grain.repositories.WorkoutSessionRepository;
import ru.univ.grain.repositories.SubscriptionRepository;
import ru.univ.grain.repositories.VisitRepository;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.mapper.VisitMapper;
import ru.univ.grain.exception.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final ClientRepository clientRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final VisitMapper visitMapper;

    private static final String VISIT_NOT_FOUND = "Визит с id %d не найден";
    private static final String CLIENT_NOT_FOUND = "Клиент с id %d не найден";
    private static final String SESSION_NOT_FOUND = "Тренировка с id %d не найдена";
    private static final String SUBSCRIPTION_NOT_FOUND = "Абонемент с id %d не найден";
    private static final String SESSION_NOT_AVAILABLE = "Тренировка недоступна для записи";
    private static final String WORKOUT_TYPE_NOT_ALLOWED = "Абонемент не подходит для этого типа тренировки";
    private static final String ALREADY_BOOKED = "Вы уже записаны на эту тренировку";
    private static final String NO_AVAILABLE_SPOTS = "Нет свободных мест на тренировке";
    private static final String INVALID_VISIT_STATUS = "Неверный статус визита для выполнения операции";

    private record VisitComponents(Client client, WorkoutSession session, Subscription subscription) { }

    private VisitComponents loadVisitComponents(final VisitDto dto) {
        final Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, dto.getClientId())));

        final WorkoutSession session = workoutSessionRepository.findById(dto.getWorkoutSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SESSION_NOT_FOUND, dto.getWorkoutSessionId())));

        final Subscription subscription = dto.getSubscriptionId() != null
                ? subscriptionRepository.findById(dto.getSubscriptionId()).orElse(null)
                : null;

        return new VisitComponents(client, session, subscription);
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getAllVisits() {
        return visitRepository.findAll().stream()
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public VisitDto getVisitById(final Long id) {
        final Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(VISIT_NOT_FOUND, id)));
        return visitMapper.toDto(visit);
    }

    @Transactional
    public VisitDto createVisit(final VisitDto dto) {
        final VisitComponents components = loadVisitComponents(dto);
        final Visit visit = visitMapper.toEntity(dto);
        visit.setClient(components.client());
        visit.setWorkoutSession(components.session());
        visit.setSubscription(components.subscription());
        final Visit saved = visitRepository.save(visit);
        return visitMapper.toDto(saved);
    }

    @Transactional
    public VisitDto updateVisit(final Long id, final VisitDto dto) {
        final Visit existing = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(VISIT_NOT_FOUND, id)));

        final VisitComponents components = loadVisitComponents(dto);

        visitMapper.updateEntity(dto, existing);
        existing.setClient(components.client());
        existing.setWorkoutSession(components.session());
        existing.setSubscription(components.subscription());

        final Visit updated = visitRepository.save(existing);
        return visitMapper.toDto(updated);
    }

    @Transactional
    public void deleteVisit(final Long id) {
        final Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(VISIT_NOT_FOUND, id)));
        visitRepository.delete(visit);
    }

    @Transactional
    public VisitDto bookWorkout(final Long clientId, final Long sessionId, final Long subscriptionId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CLIENT_NOT_FOUND, clientId)));

        final WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SESSION_NOT_FOUND, sessionId)));

        final Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SUBSCRIPTION_NOT_FOUND, subscriptionId)));

        if (session.getStatus() != WorkoutSessionStatus.SCHEDULED && session.getStatus() != WorkoutSessionStatus.CONFIRMED) {
            throw new BusinessException(SESSION_NOT_AVAILABLE);
        }

        if (!subscription.getAllowedWorkoutTypes().contains(session.getWorkoutType())) {
            throw new BusinessException(WORKOUT_TYPE_NOT_ALLOWED);
        }

        final List<Visit> existing = visitRepository.findBookedVisitsByClient(clientId);
        final boolean alreadyBooked = existing.stream()
                .anyMatch(v -> v.getWorkoutSession().getId().equals(sessionId));

        if (alreadyBooked) {
            throw new BusinessException(ALREADY_BOOKED);
        }

        final long bookedCount = visitRepository.findBookedVisitsBySession(sessionId).size();

        if (bookedCount >= session.getMaxParticipants()) {
            throw new BusinessException(NO_AVAILABLE_SPOTS);
        }

        final LocalDate today = LocalDate.now();
        final LocalDate visitDate = today.with(session.getDayOfWeek());
        final LocalDateTime visitDateTime = LocalDateTime.of(visitDate, session.getStartTime());

        final Visit visit = Visit.builder()
                .client(client)
                .workoutSession(session)
                .subscription(subscription)
                .visitTime(visitDateTime)
                .status(VisitStatus.BOOKED)
                .build();

        final Visit saved = visitRepository.save(visit);
        return visitMapper.toDto(saved);
    }

    @Transactional
    public VisitDto markAttendance(final Long visitId, final boolean attended) {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(VISIT_NOT_FOUND, visitId)));

        if (visit.getStatus() != VisitStatus.BOOKED) {
            throw new BusinessException(INVALID_VISIT_STATUS);
        }

        if (attended) {
            visit.setStatus(VisitStatus.ATTENDED);
        } else {
            visit.setStatus(VisitStatus.NO_SHOW);
        }

        final Visit updated = visitRepository.save(visit);
        return visitMapper.toDto(updated);
    }

    @Transactional
    public VisitDto cancelBooking(final Long visitId) {
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(VISIT_NOT_FOUND, visitId)));

        if (visit.getStatus() != VisitStatus.BOOKED) {
            throw new BusinessException(INVALID_VISIT_STATUS);
        }

        visit.setStatus(VisitStatus.CANCELLED);

        final Visit updated = visitRepository.save(visit);
        return visitMapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getClientVisits(final Long clientId) {
        return visitRepository.findByClientId(clientId).stream()
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getClientUpcomingVisits(final Long clientId) {
        return visitRepository.findBookedVisitsByClient(clientId).stream()
                .filter(v -> v.getVisitTime().isAfter(LocalDateTime.now()))
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getClientHistory(final Long clientId, final LocalDate from, final LocalDate to) {
        final LocalDateTime start = from.atStartOfDay();
        final LocalDateTime end = to.atTime(23, 59, 59);
        return visitRepository.findByVisitTimeBetween(start, end).stream()
                .filter(v -> v.getClient().getId().equals(clientId))
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getScheduleVisits(final Long scheduleId) {
        return visitRepository.findByWorkoutSessionId(scheduleId).stream()
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getTodayVisits() {
        final LocalDateTime start = LocalDate.now().atStartOfDay();
        final LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        return visitRepository.findByVisitTimeBetween(start, end).stream()
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getClientVisitsCount(final Long clientId, final LocalDate from, final LocalDate to) {
        final LocalDateTime start = from.atStartOfDay();
        final LocalDateTime end = to.atTime(23, 59, 59);
        return visitRepository.findByVisitTimeBetween(start, end).stream()
                .filter(v -> v.getClient().getId().equals(clientId))
                .filter(v -> v.getStatus() == VisitStatus.ATTENDED)
                .count();
    }

    @Transactional(readOnly = true)
    public long getSubscriptionUsedVisits(final Long subscriptionId) {
        return visitRepository.countAttendedBySubscriptionId(subscriptionId);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getVisitsByHourStats() {
        return visitRepository.getVisitsByHour();
    }
}

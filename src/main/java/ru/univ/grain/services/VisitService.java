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

import java.time.DayOfWeek;
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

    private record VisitComponents(Client client, WorkoutSession session, Subscription subscription) { }

    private VisitComponents loadVisitComponents(final VisitDto dto) {
        final Client client = clientRepository.findById(dto.getClientId()).orElse(null);
        final WorkoutSession session = workoutSessionRepository.findById(dto.getWorkoutSessionId()).orElse(null);
        final Subscription subscription = dto.getSubscriptionId() != null ?
                subscriptionRepository.findById(dto.getSubscriptionId()).orElse(null) : null;

        if (client == null || session == null) {
            return null;
        }

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
        return visitRepository.findById(id)
                .map(visitMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public VisitDto createVisit(final VisitDto dto) {
        final VisitComponents components = loadVisitComponents(dto);
        if (components == null) {
            return null;
        }

        final Visit visit = visitMapper.toEntity(dto);
        visit.setClient(components.client());
        visit.setWorkoutSession(components.session());
        visit.setSubscription(components.subscription());

        final Visit saved = visitRepository.save(visit);
        return visitMapper.toDto(saved);
    }

    @Transactional
    public VisitDto updateVisit(final Long id, final VisitDto dto) {
        final Visit existing = visitRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        final VisitComponents components = loadVisitComponents(dto);
        if (components == null) {
            return null;
        }

        visitMapper.updateEntity(dto, existing);
        existing.setClient(components.client());
        existing.setWorkoutSession(components.session());
        existing.setSubscription(components.subscription());

        final Visit updated = visitRepository.save(existing);
        return visitMapper.toDto(updated);
    }

    @Transactional
    public boolean deleteVisit(final Long id) {
        if (!visitRepository.existsById(id)) {
            return false;
        }
        visitRepository.deleteById(id);
        return true;
    }

    @Transactional
    public VisitDto bookWorkout(final Long clientId, final Long sessionId, final Long subscriptionId) {
        final Client client = clientRepository.findById(clientId).orElse(null);
        final WorkoutSession session = workoutSessionRepository.findById(sessionId).orElse(null);
        final Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);

        if (client == null || session == null || subscription == null) {
            return null;
        }

        if (session.getStatus() != WorkoutSessionStatus.SCHEDULED &&
                session.getStatus() != WorkoutSessionStatus.CONFIRMED) {
            return null;
        }

        if (!subscription.getAllowedWorkoutTypes().contains(session.getWorkoutType())) {
            return null;
        }

        final List<Visit> existing = visitRepository.findBookedVisitsByClient(clientId);
        final boolean alreadyBooked = existing.stream()
                .anyMatch(v -> v.getWorkoutSession().getId().equals(sessionId));

        if (alreadyBooked) {
            return null;
        }

        final long bookedCount = visitRepository.findBookedVisitsBySession(sessionId).size();

        if (bookedCount >= session.getMaxParticipants()) {
            return null;
        }

        final LocalDate today = LocalDate.now();
        final LocalDate visitDate = findNextDateForDayOfWeek(today, session.getDayOfWeek());
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

    private LocalDate findNextDateForDayOfWeek(final LocalDate from, final DayOfWeek targetDay) {
        int daysUntil = targetDay.getValue() - from.getDayOfWeek().getValue();
        if (daysUntil < 0) {
            daysUntil += 7;
        }
        return from.plusDays(daysUntil);
    }

    @Transactional
    public VisitDto markAttendance(final Long visitId, final boolean attended) {
        final Visit visit = visitRepository.findById(visitId).orElse(null);
        if (visit == null) {
            return null;
        }

        if (visit.getStatus() != VisitStatus.BOOKED) {
            return null;
        }

        visit.setStatus(attended ? VisitStatus.ATTENDED : VisitStatus.NO_SHOW);

        final Visit updated = visitRepository.save(visit);
        return visitMapper.toDto(updated);
    }

    @Transactional
    public VisitDto cancelBooking(final Long visitId) {
        final Visit visit = visitRepository.findById(visitId).orElse(null);
        if (visit == null) {
            return null;
        }

        if (visit.getStatus() != VisitStatus.BOOKED) {
            return null;
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

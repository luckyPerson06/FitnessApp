package ru.univ.grain.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.booking.AtomicBookingCounter;
import ru.univ.grain.entities.*;
import ru.univ.grain.repositories.*;
import ru.univ.grain.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingDemoService {

    private final AtomicBookingCounter atomicCounter;
    private final WorkoutSessionRepository sessionRepository;
    private final VisitRepository visitRepository;
    private final ClientRepository clientRepository;
    private final SubscriptionRepository subscriptionRepository;

    private static final Long DEMO_SESSION_ID = 4L;
    private static final int MAX_PARTICIPANTS = 12;
    private static final Long TEST_SUBSCRIPTION_ID = 3L;
    private static final String DEMO_CLIENT_EMAIL_PREFIX = "democlient";
    private static final String DEMO_CLIENT_EMAIL_SUFFIX = "@test.com";

    @PostConstruct
    public void init() {
        atomicCounter.initSessionFromDb(DEMO_SESSION_ID);
        createTestClientsIfNeeded();
    }

    private void createTestClientsIfNeeded() {
        for (int i = 1; i <= 60; i++) {
            final String email = DEMO_CLIENT_EMAIL_PREFIX + i + DEMO_CLIENT_EMAIL_SUFFIX;
            if (!clientRepository.existsByEmail(email)) {
                final Client client = Client.builder()
                        .firstName("Demo")
                        .lastName("Client" + i)
                        .email(email)
                        .password("demo123")
                        .status(ClientStatus.ACTIVE)
                        .build();
                clientRepository.save(client);
            }
        }
    }

    @Transactional
    public boolean bookWithRaceCondition(final Long clientId, final Long sessionId) throws InterruptedException {
        final WorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Session not found"));

        final long bookedCount = visitRepository.countByWorkoutSessionIdAndStatus(sessionId, VisitStatus.BOOKED);

        Thread.sleep(5L);

        if (bookedCount >= session.getMaxParticipants()) {
            return false;
        }

        final Visit visit = Visit.builder()
                .client(clientRepository.findById(clientId).orElseThrow(() -> new BusinessException("Client not found")))
                .workoutSession(session)
                .subscription(subscriptionRepository.findById(TEST_SUBSCRIPTION_ID).orElseThrow(() -> new BusinessException("Subscription not found")))
                .visitTime(LocalDateTime.now())
                .status(VisitStatus.BOOKED)
                .build();

        visitRepository.save(visit);
        atomicCounter.syncWithDatabase(sessionId);
        return true;
    }

    @Transactional
    public boolean bookWithAtomicSolution(final Long clientId, final Long sessionId) {
        final WorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Session not found"));

        if (!atomicCounter.tryBook(sessionId, session.getMaxParticipants())) {
            return false;
        }

        try {
            final Visit visit = Visit.builder()
                    .client(clientRepository.findById(clientId).orElseThrow(() -> new BusinessException("Client not found")))
                    .workoutSession(session)
                    .subscription(subscriptionRepository.findById(TEST_SUBSCRIPTION_ID).orElseThrow(() -> new BusinessException("Subscription not found")))
                    .visitTime(LocalDateTime.now())
                    .status(VisitStatus.BOOKED)
                    .build();

            visitRepository.save(visit);
            return true;
        } catch (final Exception e) {
            atomicCounter.cancelBooking(sessionId);
            return false;
        }
    }

    @Transactional
    public void resetDemoSession(final Long sessionId) {
        final List<Visit> visits = visitRepository.findByWorkoutSessionId(sessionId);
        final List<Visit> demoVisits = visits.stream()
                .filter(v -> v.getClient().getEmail().startsWith(DEMO_CLIENT_EMAIL_PREFIX))
                .toList();

        visitRepository.deleteAll(demoVisits);
        atomicCounter.reset(sessionId);
        atomicCounter.initSessionFromDb(sessionId);
    }

    public int getMaxParticipants() {
        return MAX_PARTICIPANTS;
    }
}

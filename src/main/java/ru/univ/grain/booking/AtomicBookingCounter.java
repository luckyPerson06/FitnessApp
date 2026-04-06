package ru.univ.grain.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.univ.grain.entities.VisitStatus;
import ru.univ.grain.repositories.VisitRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AtomicBookingCounter {

    private final VisitRepository visitRepository;
    private final Map<Long, AtomicInteger> counters = new ConcurrentHashMap<>();

    public void initSessionFromDb(final Long sessionId) {
        final long actualBookedCount = visitRepository.countByWorkoutSessionIdAndStatus(sessionId, VisitStatus.BOOKED);
        final AtomicInteger counter = new AtomicInteger((int) actualBookedCount);
        counters.put(sessionId, counter);
    }

    public boolean tryBook(final Long sessionId, final int maxParticipants) {
        final AtomicInteger counter = counters.computeIfAbsent(sessionId, id -> new AtomicInteger(0));
        final int current = counter.incrementAndGet();

        if (current <= maxParticipants) {
            return true;
        } else {
            counter.decrementAndGet();
            return false;
        }
    }

    public void cancelBooking(final Long sessionId) {
        final AtomicInteger counter = counters.get(sessionId);
        if (counter != null) {
            counter.decrementAndGet();
        }
    }

    public void reset(final Long sessionId) {
        final AtomicInteger counter = counters.get(sessionId);
        if (counter != null) {
            counter.set(0);
        }
    }

    public void syncWithDatabase(final Long sessionId) {
        final long actualBookedCount = visitRepository.countByWorkoutSessionIdAndStatus(sessionId, VisitStatus.BOOKED);
        final AtomicInteger counter = counters.get(sessionId);
        if (counter != null) {
            counter.set((int) actualBookedCount);
        }
    }
}

package ru.univ.grain.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.services.BookingDemoService;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.repositories.ClientRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/demo/booking")
@RequiredArgsConstructor
public class BookingDemoController {

    private final BookingDemoService demoService;
    private final ClientRepository clientRepository;

    private static final Long DEMO_SESSION_ID = 4L;
    private static final int THREAD_COUNT = 60;
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String MAX_PARTICIPANTS_KEY = "maxParticipants";
    private static final String SUCCESSFUL_BOOKINGS_KEY = "successfulBookings";
    private static final String FAILED_BOOKINGS_KEY = "failedBookings";
    private static final String OVERBOOKING_KEY = "overbooking";
    private static final String DURATION_MS_KEY = "durationMs";
    private static final String TOTAL_ATTEMPTS_KEY = "totalAttempts";
    private static final String TYPE_KEY = "type";
    private static final String EXPECTED_RESULT_KEY = "expectedResult";

    @SuppressWarnings("java:S2142")
    @PostMapping("/race")
    public ResponseEntity<Map<String, Object>> demonstrateRaceCondition() {
        demoService.resetDemoSession(DEMO_SESSION_ID);

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);

        final long startTime = System.currentTimeMillis();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            final int clientIndex = i;
            executor.submit(() -> {
                try {
                    final Long clientId = getTestClientId(clientIndex);
                    final boolean success = demoService.bookWithRaceCondition(clientId, DEMO_SESSION_ID);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (final Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        final boolean completed = awaitLatch(latch, executor);
        final long duration = System.currentTimeMillis() - startTime;

        final Map<String, Object> result = new HashMap<>();
        result.put(TYPE_KEY, "RACE CONDITION (PROBLEM)");
        result.put(SESSION_ID_KEY, DEMO_SESSION_ID);
        result.put(TOTAL_ATTEMPTS_KEY, THREAD_COUNT);
        result.put(MAX_PARTICIPANTS_KEY, demoService.getMaxParticipants());
        result.put(SUCCESSFUL_BOOKINGS_KEY, successCount.get());
        result.put(FAILED_BOOKINGS_KEY, failCount.get());
        result.put(OVERBOOKING_KEY, successCount.get() - demoService.getMaxParticipants());
        result.put(DURATION_MS_KEY, duration);
        result.put(EXPECTED_RESULT_KEY, "Should be > " + demoService.getMaxParticipants() + " bookings (overbooking)");
        result.put("completed", completed);

        return ResponseEntity.ok(result);
    }

    @SuppressWarnings("java:S2142")
    @PostMapping("/safe")
    public ResponseEntity<Map<String, Object>> demonstrateSafeBooking() {
        demoService.resetDemoSession(DEMO_SESSION_ID);

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);

        final long startTime = System.currentTimeMillis();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            final int clientIndex = i;
            executor.submit(() -> {
                try {
                    final Long clientId = getTestClientId(clientIndex);
                    final boolean success = demoService.bookWithAtomicSolution(clientId, DEMO_SESSION_ID);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (final Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        final boolean completed = awaitLatch(latch, executor);
        final long duration = System.currentTimeMillis() - startTime;

        final Map<String, Object> result = new HashMap<>();
        result.put(TYPE_KEY, "ATOMIC SOLUTION (FIX)");
        result.put(SESSION_ID_KEY, DEMO_SESSION_ID);
        result.put(TOTAL_ATTEMPTS_KEY, THREAD_COUNT);
        result.put(MAX_PARTICIPANTS_KEY, demoService.getMaxParticipants());
        result.put(SUCCESSFUL_BOOKINGS_KEY, successCount.get());
        result.put(FAILED_BOOKINGS_KEY, failCount.get());
        result.put(OVERBOOKING_KEY, successCount.get() - demoService.getMaxParticipants());
        result.put(DURATION_MS_KEY, duration);
        result.put(EXPECTED_RESULT_KEY, "Should be exactly " + demoService.getMaxParticipants() + " bookings (no overbooking)");
        result.put("completed", completed);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetDemo() {
        demoService.resetDemoSession(DEMO_SESSION_ID);
        final Map<String, String> result = new HashMap<>();
        result.put("status", "reset completed");
        result.put(SESSION_ID_KEY, String.valueOf(DEMO_SESSION_ID));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDemoStatus() {
        final Map<String, Object> result = new HashMap<>();
        result.put(SESSION_ID_KEY, DEMO_SESSION_ID);
        result.put(MAX_PARTICIPANTS_KEY, demoService.getMaxParticipants());
        result.put("threadCount", THREAD_COUNT);
        result.put("endpoints", Map.of(
                "raceCondition", "POST /api/demo/booking/race",
                "safeBooking", "POST /api/demo/booking/safe",
                "reset", "POST /api/demo/booking/reset"
        ));
        return ResponseEntity.ok(result);
    }

    private boolean awaitLatch(final CountDownLatch latch, final ExecutorService executor) {
        boolean completed = false;
        try {
            completed = latch.await(30, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
        return completed;
    }

    private Long getTestClientId(final int index) {
        final String email = "democlient" + index + "@test.com";
        final var client = clientRepository.findByEmail(email);
        if (client.isPresent()) {
            return client.get().getId();
        }
        throw new BusinessException("Test client not found: " + email);
    }
}

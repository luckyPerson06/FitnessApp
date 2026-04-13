package ru.univ.grain.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class CacheKey {
    private final CacheRegion region;
    private final String identifier;

    public static CacheKey of(CacheRegion region, String identifier) {
        return new CacheKey(region, identifier);
    }

    public static CacheKey forSessionsDate(LocalDate date) {
        return new CacheKey(CacheRegion.WORKOUT_SESSIONS, "date:" + date.toString());
    }

    public static CacheKey forSessionsTrainer(Long trainerId, LocalDate date) {
        return new CacheKey(CacheRegion.WORKOUT_SESSIONS, "trainer:" + trainerId + ":date:" + date);
    }

    public static CacheKey forWorkoutTypes() {
        return new CacheKey(CacheRegion.WORKOUT_TYPES, "all:active");
    }

    public static CacheKey forSubscriptions() {
        return new CacheKey(CacheRegion.SUBSCRIPTIONS, "all:active");
    }

    public static CacheKey forTrainers() {
        return new CacheKey(CacheRegion.TRAINERS, "all");
    }

    public static CacheKey forClubInfo() {
        return new CacheKey(CacheRegion.CLUB_INFO, "main");
    }
}

package ru.univ.grain.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheRegion {
    WORKOUT_SESSIONS("sessions", 30),
    WORKOUT_TYPES("workoutTypes", 60),
    SUBSCRIPTIONS("subscriptions", 60),
    TRAINERS("trainers", 60),
    CLUB_INFO("clubInfo", 120);

    private final String name;
    private final int ttlMinutes;
}

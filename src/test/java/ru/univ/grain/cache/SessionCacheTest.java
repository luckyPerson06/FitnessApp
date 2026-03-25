package ru.univ.grain.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.univ.grain.dto.WorkoutSessionDto;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SessionCacheTest {

    private SessionCache sessionCache;
    private SessionSearchKey testKey1;
    private SessionSearchKey testKey2;
    private Page<WorkoutSessionDto> emptyPage;
    private Page<WorkoutSessionDto> populatedPage;

    @BeforeEach
    void setUp() {
        sessionCache = new SessionCache();

        testKey1 = new SessionSearchKey("Смирнова", DayOfWeek.MONDAY, 0, 10, "startTime");
        testKey2 = new SessionSearchKey("Иванова", DayOfWeek.WEDNESDAY, 1, 5, "startTime");

        emptyPage = new PageImpl<>(new ArrayList<>());
        populatedPage = new PageImpl<>(List.of(new WorkoutSessionDto()));
    }

    @Test
    void put_ShouldStoreValueInCache() {
        sessionCache.put(testKey1, emptyPage);

        assertThat(sessionCache.size()).isEqualTo(1);
        assertThat(sessionCache.get(testKey1)).isEqualTo(emptyPage);
    }

    @Test
    void get_ShouldReturnNull_WhenKeyNotExists() {
        Page<WorkoutSessionDto> result = sessionCache.get(testKey1);

        assertThat(result).isNull();
    }

    @Test
    void get_ShouldReturnValue_WhenKeyExists() {
        sessionCache.put(testKey1, emptyPage);

        Page<WorkoutSessionDto> result = sessionCache.get(testKey1);

        assertThat(result).isEqualTo(emptyPage);
    }

    @Test
    void remove_ShouldRemoveValue_WhenKeyExists() {
        sessionCache.put(testKey1, emptyPage);
        sessionCache.put(testKey2, populatedPage);

        sessionCache.remove(testKey1);

        assertThat(sessionCache.size()).isEqualTo(1);
        assertThat(sessionCache.get(testKey1)).isNull();
        assertThat(sessionCache.get(testKey2)).isEqualTo(populatedPage);
    }

    @Test
    void clearByTrainerLastName_ShouldRemoveAllEntriesWithMatchingLastName() {
        // Создаем ключи с разными именами
        SessionSearchKey smirnovaKeyMonday = new SessionSearchKey("Смирнова", DayOfWeek.MONDAY, 0, 10, "startTime");
        SessionSearchKey smirnovaKeyFriday = new SessionSearchKey("Смирнова", DayOfWeek.FRIDAY, 0, 10, "startTime");
        SessionSearchKey ivanovaKey = new SessionSearchKey("Иванова", DayOfWeek.WEDNESDAY, 0, 10, "startTime");

        sessionCache.put(smirnovaKeyMonday, emptyPage);
        sessionCache.put(smirnovaKeyFriday, emptyPage);
        sessionCache.put(ivanovaKey, populatedPage);

        sessionCache.clearByTrainerLastName("Смирнова");

        assertThat(sessionCache.size()).isEqualTo(1);
        assertThat(sessionCache.get(smirnovaKeyMonday)).isNull();
        assertThat(sessionCache.get(smirnovaKeyFriday)).isNull();
        assertThat(sessionCache.get(ivanovaKey)).isEqualTo(populatedPage);
    }

    @Test
    void size_ShouldReturnCorrectNumberOfEntries() {
        assertThat(sessionCache.size()).isZero();

        sessionCache.put(testKey1, emptyPage);
        assertThat(sessionCache.size()).isEqualTo(1);

        sessionCache.put(testKey2, populatedPage);
        assertThat(sessionCache.size()).isEqualTo(2);

        sessionCache.remove(testKey1);
        assertThat(sessionCache.size()).isEqualTo(1);
    }

    @Test
    void put_ShouldOverwriteExistingKey() {
        Page<WorkoutSessionDto> oldPage = new PageImpl<>(new ArrayList<>());
        Page<WorkoutSessionDto> newPage = new PageImpl<>(List.of(new WorkoutSessionDto()));

        sessionCache.put(testKey1, oldPage);
        sessionCache.put(testKey1, newPage);

        assertThat(sessionCache.size()).isEqualTo(1);
        assertThat(sessionCache.get(testKey1)).isEqualTo(newPage);
        assertThat(sessionCache.get(testKey1)).isNotEqualTo(oldPage);
    }

    @Test
    void clearByTrainerLastName_ShouldDoNothing_WhenNoMatchingKeys() {
        sessionCache.put(testKey1, emptyPage);
        sessionCache.put(testKey2, populatedPage);

        sessionCache.clearByTrainerLastName("Петрова");

        assertThat(sessionCache.size()).isEqualTo(2);
        assertThat(sessionCache.get(testKey1)).isEqualTo(emptyPage);
        assertThat(sessionCache.get(testKey2)).isEqualTo(populatedPage);
    }
}
package ru.univ.grain.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.DayOfWeek;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class SessionSearchKey {
    private final Long trainerId;
    private final DayOfWeek dayOfWeek;
    private final int page;
    private final int size;
    private final String sortField;
}

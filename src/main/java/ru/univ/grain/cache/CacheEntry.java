package ru.univ.grain.cache;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CacheEntry<T> {
    private final T value;
    private final LocalDateTime expiresAt;

    public CacheEntry(T value, int ttlMinutes) {
        this.value = value;
        this.expiresAt = LocalDateTime.now().plusMinutes(ttlMinutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

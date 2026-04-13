package ru.univ.grain.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AppCache {

    private final Map<CacheKey, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(CacheKey key) {
        CacheEntry<?> entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            log.debug("Cache expired for key: {}", key);
            return null;
        }
        log.debug("Cache hit for key: {}", key);
        return (T) entry.getValue();
    }

    public <T> void put(CacheKey key, T value) {
        CacheEntry<T> entry = new CacheEntry<>(value, key.getRegion().getTtlMinutes());
        cache.put(key, entry);
        log.debug("Cache put for key: {}", key);
    }

    public void evict(CacheKey key) {
        cache.remove(key);
        log.debug("Cache evicted for key: {}", key);
    }

    public void clearRegion(CacheRegion region) {
        cache.keySet().removeIf(key -> key.getRegion() == region);
        log.info("Cache region cleared: {}", region.getName());
    }

    public void clearAll() {
        cache.clear();
        log.info("All cache cleared");
    }

    public void evictSessionsForDate(LocalDate date) {
        cache.keySet().removeIf(key ->
                key.getRegion() == CacheRegion.WORKOUT_SESSIONS &&
                        key.getIdentifier().contains("date:" + date)
        );
        log.debug("Evicted sessions cache for date: {}", date);
    }

    public void evictSessionsForTrainer(Long trainerId) {
        cache.keySet().removeIf(key ->
                key.getRegion() == CacheRegion.WORKOUT_SESSIONS &&
                        key.getIdentifier().contains("trainer:" + trainerId)
        );
        log.debug("Evicted sessions cache for trainer: {}", trainerId);
    }

    public int size() {
        return cache.size();
    }

    public Map<CacheRegion, Integer> getStats() {
        Map<CacheRegion, Integer> stats = new ConcurrentHashMap<>();
        for (CacheRegion region : CacheRegion.values()) {
            stats.put(region, 0);
        }
        cache.keySet().forEach(key -> stats.merge(key.getRegion(), 1, Integer::sum));
        return stats;
    }
}

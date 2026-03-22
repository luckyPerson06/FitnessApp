package ru.univ.grain.cache;

import org.springframework.stereotype.Component;
import ru.univ.grain.dto.WorkoutSessionDto;
import org.springframework.data.domain.Page;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionCache {

    private final Map<SessionSearchKey, Page<WorkoutSessionDto>> cache = new ConcurrentHashMap<>();

    public Page<WorkoutSessionDto> get(SessionSearchKey key) {
        return cache.get(key);
    }

    public void put(SessionSearchKey key, Page<WorkoutSessionDto> value) {
        cache.put(key, value);
    }

    public void remove(SessionSearchKey key) {
        cache.remove(key);
    }

    public void clearByTrainerLastName(String trainerLastName) {
        cache.keySet().removeIf(key -> key.getTrainerLastName().equals(trainerLastName));
    }

    public int size() {
        return cache.size();
    }

}

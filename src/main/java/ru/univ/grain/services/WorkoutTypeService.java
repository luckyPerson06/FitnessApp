package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.cache.AppCache;
import ru.univ.grain.cache.CacheKey;
import ru.univ.grain.cache.CacheRegion;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.entities.WorkoutCategory;
import ru.univ.grain.entities.WorkoutType;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.WorkoutTypeMapper;
import ru.univ.grain.repositories.WorkoutTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutTypeService {

    private final WorkoutTypeRepository workoutTypeRepository;
    private final WorkoutTypeMapper workoutTypeMapper;
    private final AppCache appCache;

    private static final String WORKOUT_TYPE_NOT_FOUND = "Тип тренировки с id %d не найден";
    private static final String WORKOUT_TYPE_NAME_EXISTS = "Тип тренировки с названием '%s' уже существует";
    private static final String WORKOUT_TYPE_IN_USE = "Невозможно удалить тип тренировки: есть связанные тренеры, абонементы или тренировки";

    @Transactional(readOnly = true)
    public List<WorkoutTypeDto> getAllWorkoutTypes() {
        return workoutTypeRepository.findAll().stream()
                .map(workoutTypeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutTypeDto getWorkoutTypeById(final Long id) {
        final WorkoutType workoutType = workoutTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(WORKOUT_TYPE_NOT_FOUND, id)));
        return workoutTypeMapper.toDto(workoutType);
    }

    @Transactional(readOnly = true)
    public WorkoutTypeDto getWorkoutTypeByName(final String name) {
        final WorkoutType workoutType = workoutTypeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Тип тренировки с названием '" + name + "' не найден"));
        return workoutTypeMapper.toDto(workoutType);
    }

    @Transactional(readOnly = true)
    public List<WorkoutTypeDto> getActiveWorkoutTypes() {
        final CacheKey key = CacheKey.forWorkoutTypes();

        final List<WorkoutTypeDto> cached = appCache.get(key);
        if (cached != null) {
            return cached;
        }

        final List<WorkoutTypeDto> result = workoutTypeRepository.findByIsActiveTrue().stream()
                .map(workoutTypeMapper::toDto)
                .toList();

        appCache.put(key, result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<WorkoutTypeDto> getWorkoutTypesByCategory(final WorkoutCategory category) {
        return workoutTypeRepository.findByCategory(category).stream()
                .map(workoutTypeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutTypeDto> getWorkoutTypesByTrainer(final Long trainerId) {
        return workoutTypeRepository.findByTrainerId(trainerId).stream()
                .map(workoutTypeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByName(final String name) {
        return workoutTypeRepository.existsByNameIgnoreCase(name);
    }

    @Transactional
    public WorkoutTypeDto createWorkoutType(final WorkoutTypeDto dto) {
        workoutTypeRepository.findByNameIgnoreCase(dto.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            String.format(WORKOUT_TYPE_NAME_EXISTS, dto.getName()));
                });

        final WorkoutType workoutType = workoutTypeMapper.toEntity(dto);
        final WorkoutType saved = workoutTypeRepository.save(workoutType);

        appCache.clearRegion(CacheRegion.WORKOUT_TYPES);
        return workoutTypeMapper.toDto(saved);
    }

    @Transactional
    public WorkoutTypeDto updateWorkoutType(final Long id, final WorkoutTypeDto dto) {
        final WorkoutType existing = workoutTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(WORKOUT_TYPE_NOT_FOUND, id)));

        if (!existing.getName().equalsIgnoreCase(dto.getName())) {
            workoutTypeRepository.findByNameIgnoreCase(dto.getName())
                    .ifPresent(workoutTypeWithSameName -> {
                        throw new DuplicateResourceException(
                                String.format(WORKOUT_TYPE_NAME_EXISTS, dto.getName()));
                    });
        }

        workoutTypeMapper.updateEntity(dto, existing);
        final WorkoutType updated = workoutTypeRepository.save(existing);

        appCache.clearRegion(CacheRegion.WORKOUT_TYPES);
        return workoutTypeMapper.toDto(updated);
    }

    @Transactional
    public void deactivateWorkoutType(final Long id) {
        final WorkoutType workoutType = workoutTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(WORKOUT_TYPE_NOT_FOUND, id)));

        workoutType.setIsActive(false);
        workoutTypeRepository.save(workoutType);
        appCache.clearRegion(CacheRegion.WORKOUT_TYPES);
    }

    @Transactional
    public void deleteWorkoutType(final Long id) {
        final WorkoutType workoutType = workoutTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(WORKOUT_TYPE_NOT_FOUND, id)));

        if (!workoutType.getTrainers().isEmpty() ||
                !workoutType.getSubscriptions().isEmpty() ||
                !workoutType.getWorkoutSessions().isEmpty()) {
            throw new BusinessException(WORKOUT_TYPE_IN_USE);
        }

        workoutTypeRepository.delete(workoutType);
        appCache.clearRegion(CacheRegion.WORKOUT_TYPES);
    }
}

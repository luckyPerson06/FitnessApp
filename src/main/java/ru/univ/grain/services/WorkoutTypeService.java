package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.entities.WorkoutCategory;
import ru.univ.grain.entities.WorkoutType;
import ru.univ.grain.repositories.WorkoutTypeRepository;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.mapper.WorkoutTypeMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutTypeService {

    private final WorkoutTypeRepository workoutTypeRepository;
    private final WorkoutTypeMapper workoutTypeMapper;

    @Transactional(readOnly = true)
    public List<WorkoutTypeDto> getAllWorkoutTypes() {
        return workoutTypeRepository.findAll().stream()
                .map(workoutTypeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutTypeDto getWorkoutTypeById(final Long id) {
        return workoutTypeRepository.findById(id)
                .map(workoutTypeMapper::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public WorkoutTypeDto getWorkoutTypeByName(final String name) {
        final WorkoutType workoutType = workoutTypeRepository.findByNameIgnoreCase(name);
        if (workoutType == null) {
            return null;
        }
        return workoutTypeMapper.toDto(workoutType);
    }

    @Transactional
    public WorkoutTypeDto createWorkoutType(final WorkoutTypeDto dto) {
        final WorkoutType existing = workoutTypeRepository.findByNameIgnoreCase(dto.getName());
        if (existing != null) {
            return null;
        }

        final WorkoutType workoutType = workoutTypeMapper.toEntity(dto);
        final WorkoutType saved = workoutTypeRepository.save(workoutType);
        return workoutTypeMapper.toDto(saved);
    }

    @Transactional
    public WorkoutTypeDto updateWorkoutType(final Long id, final WorkoutTypeDto dto) {
        final WorkoutType existing = workoutTypeRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        if (!existing.getName().equalsIgnoreCase(dto.getName())) {
            final WorkoutType workoutTypeWithSameName = workoutTypeRepository.findByNameIgnoreCase(dto.getName());
            if (workoutTypeWithSameName != null) {
                return null;
            }
        }

        workoutTypeMapper.updateEntity(dto, existing);
        final WorkoutType updated = workoutTypeRepository.save(existing);
        return workoutTypeMapper.toDto(updated);
    }

    @Transactional
    public boolean deactivateWorkoutType(final Long id) {
        final WorkoutType workoutType = workoutTypeRepository.findById(id).orElse(null);
        if (workoutType == null) {
            return false;
        }

        workoutType.setIsActive(false);
        workoutTypeRepository.save(workoutType);
        return true;
    }

    @Transactional(readOnly = true)
    public List<WorkoutTypeDto> getActiveWorkoutTypes() {
        return workoutTypeRepository.findByIsActiveTrue().stream()
                .map(workoutTypeMapper::toDto)
                .toList();
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
    public List<WorkoutTypeDto> getWorkoutTypesBySubscription(final Long subscriptionId) {
        return workoutTypeRepository.findBySubscriptionId(subscriptionId).stream()
                .map(workoutTypeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByName(final String name) {
        return workoutTypeRepository.existsByNameIgnoreCase(name);
    }

    @Transactional
    public boolean deleteWorkoutType(final Long id) {
        if (!workoutTypeRepository.existsById(id)) {
            return false;
        }


        final WorkoutType workoutType = workoutTypeRepository.findById(id).orElse(null);
        if (workoutType != null &&
                (!workoutType.getTrainers().isEmpty() ||
                        !workoutType.getSubscriptions().isEmpty() ||
                        !workoutType.getWorkoutSessions().isEmpty())) {
            return false;
        }

        workoutTypeRepository.deleteById(id);
        return true;
    }

}

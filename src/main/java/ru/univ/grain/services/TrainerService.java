package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.cache.AppCache;
import ru.univ.grain.cache.CacheKey;
import ru.univ.grain.cache.CacheRegion;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.entities.Trainer;
import ru.univ.grain.entities.TrainerStatus;
import ru.univ.grain.entities.WorkoutType;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.TrainerMapper;
import ru.univ.grain.repositories.TrainerRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final TrainerMapper trainerMapper;
    private final AppCache appCache;

    private static final String TRAINER_NOT_FOUND = "Тренер с id %d не найден";
    private static final String WORKOUT_TYPE_NOT_FOUND = "Тип тренировки с id %d не найден";
    private static final String SPECIALIZATION_NOT_FOUND = "У тренера нет такой специализации";

    @Transactional(readOnly = true)
    public List<TrainerDto> getAllTrainers() {
        final CacheKey key = CacheKey.forTrainers();

        final List<TrainerDto> cached = appCache.get(key);
        if (cached != null) {
            return cached;
        }

        final List<TrainerDto> result = trainerRepository.findAll().stream()
                .map(trainerMapper::toDto)
                .toList();

        appCache.put(key, result);
        return result;
    }

    @Transactional(readOnly = true)
    public TrainerDto getTrainerById(final Long id) {
        final Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, id)));
        return trainerMapper.toDto(trainer);
    }

    @Transactional(readOnly = true)
    public List<TrainerDto> getTrainersByStatus(final TrainerStatus status) {
        return trainerRepository.findByStatus(status).stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerDto> getActiveTrainers() {
        return trainerRepository.findByStatusIn(List.of(TrainerStatus.ACTIVE)).stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerDto> getTrainersWithSessionOnDay(final DayOfWeek dayOfWeek) {
        return trainerRepository.findTrainersWithSessionOnDay(dayOfWeek).stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    @Transactional
    public TrainerDto createTrainer(final TrainerDto dto) {
        final Trainer trainer = trainerMapper.toEntity(dto);
        final Trainer saved = trainerRepository.save(trainer);

        appCache.clearRegion(CacheRegion.TRAINERS);
        return trainerMapper.toDto(saved);
    }

    @Transactional
    public TrainerDto updateTrainer(final Long id, final TrainerDto dto) {
        final Trainer existing = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, id)));

        trainerMapper.updateEntity(dto, existing);
        final Trainer updated = trainerRepository.save(existing);

        appCache.clearRegion(CacheRegion.TRAINERS);
        return trainerMapper.toDto(updated);
    }

    @Transactional
    public void deleteTrainer(final Long id) {
        final Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, id)));

        trainerRepository.delete(trainer);
        appCache.clearRegion(CacheRegion.TRAINERS);
    }

    @Transactional
    public void addSpecialization(final Long trainerId, final Long workoutTypeId) {
        final Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, trainerId)));

        final WorkoutType workoutType = workoutTypeRepository.findById(workoutTypeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(WORKOUT_TYPE_NOT_FOUND, workoutTypeId)));

        if (!trainer.getSpecializations().contains(workoutType)) {
            trainer.getSpecializations().add(workoutType);
            trainerRepository.save(trainer);
            appCache.clearRegion(CacheRegion.TRAINERS);
        }
    }

    @Transactional
    public void removeSpecialization(final Long trainerId, final Long workoutTypeId) {
        final Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, trainerId)));

        final boolean removed = trainer.getSpecializations().removeIf(
                wt -> wt.getId().equals(workoutTypeId));

        if (!removed) {
            throw new ResourceNotFoundException(SPECIALIZATION_NOT_FOUND);
        }

        trainerRepository.save(trainer);
        appCache.clearRegion(CacheRegion.TRAINERS);
    }
}

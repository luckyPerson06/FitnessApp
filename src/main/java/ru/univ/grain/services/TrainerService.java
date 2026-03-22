package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.entities.Trainer;
import ru.univ.grain.entities.TrainerStatus;
import ru.univ.grain.entities.WorkoutType;
import ru.univ.grain.repositories.TrainerRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.mapper.TrainerMapper;
import ru.univ.grain.exception.*;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final TrainerMapper trainerMapper;


    private static final String TRAINER_NOT_FOUND = "Тренер с id %d не найден";
    private static final String WORKOUT_TYPE_NOT_FOUND = "Тип тренировки с id %d не найден";
    private static final String SPECIALIZATION_NOT_FOUND = "У тренера нет такой специализации";

    @Transactional(readOnly = true)
    public List<TrainerDto> getAllTrainers() {
        return trainerRepository.findAll().stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainerDto getTrainerById(final Long id) {
        final Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, id)));
        return trainerMapper.toDto(trainer);
    }

    @Transactional
    public TrainerDto createTrainer(final TrainerDto dto) {
        final Trainer trainer = trainerMapper.toEntity(dto);
        final Trainer saved = trainerRepository.save(trainer);
        return trainerMapper.toDto(saved);
    }

    @Transactional
    public TrainerDto updateTrainer(final Long id, final TrainerDto dto) {
        final Trainer existing = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, id)));

        trainerMapper.updateEntity(dto, existing);
        final Trainer updated = trainerRepository.save(existing);
        return trainerMapper.toDto(updated);
    }

    @Transactional
    public void deleteTrainer(final Long id) {
        final Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(TRAINER_NOT_FOUND, id)));
        trainerRepository.delete(trainer);
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
    public List<TrainerDto> getTrainersBySpecialization(final String specializationName) {
        return trainerRepository.findBySpecializationName(specializationName).stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerDto> getTrainersWithSessionOnDay(final DayOfWeek dayOfWeek) {
        return trainerRepository.findTrainersWithSessionOnDay(dayOfWeek).stream()
                .map(trainerMapper::toDto)
                .toList();
    }

    private int[] calculateStats(List<Trainer> trainers) {
        int totalSpecializations = 0;
        int totalSessions = 0;

        for (Trainer trainer : trainers) {
            totalSpecializations += trainer.getSpecializations().size();
            totalSessions += trainer.getWorkoutSessions().size();
        }

        return new int[]{trainers.size(), totalSpecializations, totalSessions};
    }

    @Transactional(readOnly = true)
    public int[] demonstrateNPlus1Problem() {
        return calculateStats(trainerRepository.findAll());
    }

    @Transactional(readOnly = true)
    public int[] demonstrateSolution() {
        return calculateStats(trainerRepository.findAllWithDetails());
    }
}

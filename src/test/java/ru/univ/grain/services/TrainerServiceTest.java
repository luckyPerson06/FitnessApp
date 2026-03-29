package ru.univ.grain.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.univ.grain.dto.TrainerDto;
import ru.univ.grain.entities.Trainer;
import ru.univ.grain.entities.TrainerStatus;
import ru.univ.grain.entities.WorkoutType;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.TrainerMapper;
import ru.univ.grain.repositories.TrainerRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private WorkoutTypeRepository workoutTypeRepository;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void getAllTrainers_ShouldReturnList() {
        Trainer trainer1 = new Trainer();
        trainer1.setId(1L);
        Trainer trainer2 = new Trainer();
        trainer2.setId(2L);

        TrainerDto dto1 = new TrainerDto();
        TrainerDto dto2 = new TrainerDto();

        when(trainerRepository.findAll()).thenReturn(List.of(trainer1, trainer2));
        when(trainerMapper.toDto(trainer1)).thenReturn(dto1);
        when(trainerMapper.toDto(trainer2)).thenReturn(dto2);

        List<TrainerDto> result = trainerService.getAllTrainers();

        assertThat(result).hasSize(2);
    }

    @Test
    void getTrainerById_ShouldReturnTrainer_WhenExists() {
        Long id = 1L;
        Trainer trainer = new Trainer();
        trainer.setId(id);
        trainer.setFirstName("Анна");

        TrainerDto dto = new TrainerDto();
        dto.setFirstName("Анна");

        when(trainerRepository.findById(id)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(dto);

        TrainerDto result = trainerService.getTrainerById(id);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Анна");
    }

    @Test
    void getTrainerById_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(trainerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void createTrainer_ShouldReturnTrainer_WhenValid() {
        TrainerDto dto = new TrainerDto();
        dto.setFirstName("Анна");
        dto.setLastName("Смирнова");

        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setFirstName(dto.getFirstName());

        TrainerDto responseDto = new TrainerDto();
        responseDto.setFirstName(dto.getFirstName());

        when(trainerMapper.toEntity(dto)).thenReturn(trainer);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(trainerMapper.toDto(trainer)).thenReturn(responseDto);

        TrainerDto result = trainerService.createTrainer(dto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Анна");
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_ShouldUpdateTrainer_WhenValid() {
        Long id = 1L;
        TrainerDto dto = new TrainerDto();
        dto.setFirstName("Анна");
        dto.setLastName("Смирнова");

        Trainer existing = new Trainer();
        existing.setId(id);
        existing.setFirstName("Анна");

        Trainer updated = new Trainer();
        updated.setId(id);
        updated.setFirstName("Анна");

        TrainerDto responseDto = new TrainerDto();
        responseDto.setFirstName("Анна");

        when(trainerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(updated);
        when(trainerMapper.toDto(updated)).thenReturn(responseDto);

        TrainerDto result = trainerService.updateTrainer(id, dto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Анна");
        verify(trainerMapper).updateEntity(dto, existing);
        verify(trainerRepository).save(existing);
    }

    @Test
    void updateTrainer_ShouldThrowException_WhenNotFound() {
        Long id = 999L;
        TrainerDto dto = new TrainerDto();

        when(trainerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateTrainer(id, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void deleteTrainer_ShouldDeleteTrainer_WhenExists() {
        Long id = 1L;
        Trainer trainer = new Trainer();
        trainer.setId(id);
        trainer.setSpecializations(new HashSet<>());

        when(trainerRepository.findById(id)).thenReturn(Optional.of(trainer));

        trainerService.deleteTrainer(id);

        verify(trainerRepository).delete(trainer);
    }

    @Test
    void deleteTrainer_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(trainerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.deleteTrainer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(trainerRepository, never()).delete(any());
    }

    @Test
    void addSpecialization_ShouldAdd_WhenNotExists() {
        Long trainerId = 1L;
        Long workoutTypeId = 1L;

        Trainer trainer = new Trainer();
        trainer.setId(trainerId);
        trainer.setSpecializations(new HashSet<>());

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(workoutTypeId);

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(workoutTypeRepository.findById(workoutTypeId)).thenReturn(Optional.of(workoutType));

        trainerService.addSpecialization(trainerId, workoutTypeId);

        assertThat(trainer.getSpecializations()).contains(workoutType);
        verify(trainerRepository).save(trainer);
    }

    @Test
    void addSpecialization_ShouldThrowException_WhenTrainerNotFound() {
        Long trainerId = 999L;
        Long workoutTypeId = 1L;

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.addSpecialization(trainerId, workoutTypeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void addSpecialization_ShouldThrowException_WhenWorkoutTypeNotFound() {
        Long trainerId = 1L;
        Long workoutTypeId = 999L;

        Trainer trainer = new Trainer();
        trainer.setId(trainerId);

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(workoutTypeRepository.findById(workoutTypeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.addSpecialization(trainerId, workoutTypeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void removeSpecialization_ShouldRemove_WhenExists() {
        Long trainerId = 1L;
        Long workoutTypeId = 1L;

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(workoutTypeId);

        Set<WorkoutType> specializations = new HashSet<>();
        specializations.add(workoutType);

        Trainer trainer = new Trainer();
        trainer.setId(trainerId);
        trainer.setSpecializations(specializations);

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));

        trainerService.removeSpecialization(trainerId, workoutTypeId);

        assertThat(trainer.getSpecializations()).doesNotContain(workoutType);
        verify(trainerRepository).save(trainer);
    }

    @Test
    void removeSpecialization_ShouldThrowException_WhenNotExists() {
        Long trainerId = 1L;
        Long workoutTypeId = 999L;

        Trainer trainer = new Trainer();
        trainer.setId(trainerId);
        trainer.setSpecializations(new HashSet<>());

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> trainerService.removeSpecialization(trainerId, workoutTypeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("нет такой специализации");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getTrainersByStatus_ShouldReturnList() {
        TrainerStatus status = TrainerStatus.ACTIVE;
        Trainer trainer = new Trainer();
        trainer.setStatus(status);

        TrainerDto dto = new TrainerDto();

        when(trainerRepository.findByStatus(status)).thenReturn(List.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(dto);

        List<TrainerDto> result = trainerService.getTrainersByStatus(status);

        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveTrainers_ShouldReturnList() {
        Trainer trainer = new Trainer();
        trainer.setStatus(TrainerStatus.ACTIVE);

        TrainerDto dto = new TrainerDto();

        when(trainerRepository.findByStatusIn(List.of(TrainerStatus.ACTIVE))).thenReturn(List.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(dto);

        List<TrainerDto> result = trainerService.getActiveTrainers();

        assertThat(result).hasSize(1);
    }

    @Test
    void getTrainersBySpecialization_ShouldReturnList() {
        String specialization = "Йога";
        Trainer trainer = new Trainer();

        TrainerDto dto = new TrainerDto();

        when(trainerRepository.findBySpecializationName(specialization)).thenReturn(List.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(dto);

        List<TrainerDto> result = trainerService.getTrainersBySpecialization(specialization);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTrainersWithSessionOnDay_ShouldReturnList() {
        DayOfWeek day = DayOfWeek.MONDAY;
        Trainer trainer = new Trainer();

        TrainerDto dto = new TrainerDto();

        when(trainerRepository.findTrainersWithSessionOnDay(day)).thenReturn(List.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(dto);

        List<TrainerDto> result = trainerService.getTrainersWithSessionOnDay(day);

        assertThat(result).hasSize(1);
    }

    @Test
    void demonstrateNPlus1Problem_ShouldReturnStats() {
        Trainer trainer = new Trainer();
        trainer.setSpecializations(Set.of(new WorkoutType()));
        trainer.setWorkoutSessions(Set.of());

        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        int[] stats = trainerService.demonstrateNPlus1Problem();

        assertThat(stats).hasSize(3);
        assertThat(stats[0]).isEqualTo(1);
        assertThat(stats[1]).isEqualTo(1);
        assertThat(stats[2]).isZero();
    }

    @Test
    void demonstrateSolution_ShouldReturnStats() {
        Trainer trainer = new Trainer();
        trainer.setSpecializations(Set.of(new WorkoutType()));
        trainer.setWorkoutSessions(Set.of());

        when(trainerRepository.findAllWithDetails()).thenReturn(List.of(trainer));

        int[] stats = trainerService.demonstrateSolution();

        assertThat(stats).hasSize(3);
        assertThat(stats[0]).isEqualTo(1);
        assertThat(stats[1]).isEqualTo(1);
        assertThat(stats[2]).isZero();
    }

    @Test
    void getAllTrainers_ShouldReturnEmptyList_WhenNoTrainers() {
        when(trainerRepository.findAll()).thenReturn(List.of());

        List<TrainerDto> result = trainerService.getAllTrainers();

        assertThat(result).isEmpty();
    }



    @Test
    void addSpecialization_ShouldNotAdd_WhenAlreadyExists() {
        Long trainerId = 1L;
        Long workoutTypeId = 1L;

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(workoutTypeId);

        Set<WorkoutType> specializations = new HashSet<>();
        specializations.add(workoutType);

        Trainer trainer = new Trainer();
        trainer.setId(trainerId);
        trainer.setSpecializations(specializations);

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(workoutTypeRepository.findById(workoutTypeId)).thenReturn(Optional.of(workoutType));

        trainerService.addSpecialization(trainerId, workoutTypeId);

        assertThat(trainer.getSpecializations()).hasSize(1);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void removeSpecialization_ShouldThrowException_WhenTrainerNotFound() {
        Long trainerId = 999L;
        Long workoutTypeId = 1L;

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.removeSpecialization(trainerId, workoutTypeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getTrainersByStatus_ShouldReturnEmptyList_WhenStatusIsNull() {
        List<TrainerDto> result = trainerService.getTrainersByStatus(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getTrainersByStatus_ShouldReturnEmptyList_WhenNoTrainers() {
        TrainerStatus status = TrainerStatus.VACATION;

        when(trainerRepository.findByStatus(status)).thenReturn(List.of());

        List<TrainerDto> result = trainerService.getTrainersByStatus(status);

        assertThat(result).isEmpty();
    }


    @Test
    void getActiveTrainers_ShouldReturnEmptyList_WhenNoActiveTrainers() {
        when(trainerRepository.findByStatusIn(List.of(TrainerStatus.ACTIVE))).thenReturn(List.of());

        List<TrainerDto> result = trainerService.getActiveTrainers();

        assertThat(result).isEmpty();
        verify(trainerRepository).findByStatusIn(List.of(TrainerStatus.ACTIVE));
    }

    @Test
    void getTrainersBySpecialization_ShouldReturnEmptyList_WhenSpecializationNameIsNull() {
        List<TrainerDto> result = trainerService.getTrainersBySpecialization(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getTrainersBySpecialization_ShouldReturnEmptyList_WhenSpecializationNameIsBlank() {
        List<TrainerDto> result = trainerService.getTrainersBySpecialization("");
        assertThat(result).isEmpty();
    }

    @Test
    void getTrainersBySpecialization_ShouldReturnEmptyList_WhenNoTrainers() {
        String specialization = "Несуществующая";

        when(trainerRepository.findBySpecializationName(specialization)).thenReturn(List.of());

        List<TrainerDto> result = trainerService.getTrainersBySpecialization(specialization);

        assertThat(result).isEmpty();
    }

    @Test
    void getTrainersWithSessionOnDay_ShouldReturnEmptyList_WhenDayOfWeekIsNull() {
        List<TrainerDto> result = trainerService.getTrainersWithSessionOnDay(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getTrainersWithSessionOnDay_ShouldReturnEmptyList_WhenNoTrainers() {
        DayOfWeek day = DayOfWeek.MONDAY;

        when(trainerRepository.findTrainersWithSessionOnDay(day)).thenReturn(List.of());

        List<TrainerDto> result = trainerService.getTrainersWithSessionOnDay(day);

        assertThat(result).isEmpty();
    }

    @Test
    void demonstrateNPlus1Problem_ShouldReturnZeroStats_WhenNoTrainers() {
        when(trainerRepository.findAll()).thenReturn(List.of());

        int[] stats = trainerService.demonstrateNPlus1Problem();

        assertThat(stats[0]).isZero();
        assertThat(stats[1]).isZero();
        assertThat(stats[2]).isZero();
    }

    @Test
    void demonstrateSolution_ShouldReturnZeroStats_WhenNoTrainers() {
        when(trainerRepository.findAllWithDetails()).thenReturn(List.of());

        int[] stats = trainerService.demonstrateSolution();

        assertThat(stats[0]).isZero();
        assertThat(stats[1]).isZero();
        assertThat(stats[2]).isZero();
    }
}

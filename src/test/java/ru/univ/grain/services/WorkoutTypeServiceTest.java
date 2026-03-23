package ru.univ.grain.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.univ.grain.dto.WorkoutTypeDto;
import ru.univ.grain.entities.*;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.WorkoutTypeMapper;
import ru.univ.grain.repositories.WorkoutTypeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutTypeServiceTest {

    @Mock
    private WorkoutTypeRepository workoutTypeRepository;

    @Mock
    private WorkoutTypeMapper workoutTypeMapper;

    @InjectMocks
    private WorkoutTypeService workoutTypeService;

    @Test
    void getAllWorkoutTypes_ShouldReturnList() {
        WorkoutType type1 = new WorkoutType();
        WorkoutType type2 = new WorkoutType();
        WorkoutTypeDto dto1 = new WorkoutTypeDto();
        WorkoutTypeDto dto2 = new WorkoutTypeDto();

        when(workoutTypeRepository.findAll()).thenReturn(List.of(type1, type2));
        when(workoutTypeMapper.toDto(type1)).thenReturn(dto1);
        when(workoutTypeMapper.toDto(type2)).thenReturn(dto2);

        List<WorkoutTypeDto> result = workoutTypeService.getAllWorkoutTypes();

        assertThat(result).hasSize(2);
    }

    @Test
    void getWorkoutTypeById_ShouldReturnWorkoutType_WhenExists() {
        Long id = 1L;
        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(id);
        WorkoutTypeDto dto = new WorkoutTypeDto();

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.of(workoutType));
        when(workoutTypeMapper.toDto(workoutType)).thenReturn(dto);

        WorkoutTypeDto result = workoutTypeService.getWorkoutTypeById(id);

        assertThat(result).isNotNull();
    }

    @Test
    void getWorkoutTypeById_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutTypeService.getWorkoutTypeById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getWorkoutTypeByName_ShouldReturnWorkoutType_WhenExists() {
        String name = "Йога";
        WorkoutType workoutType = new WorkoutType();
        workoutType.setName(name);
        WorkoutTypeDto dto = new WorkoutTypeDto();

        when(workoutTypeRepository.findByNameIgnoreCase(name)).thenReturn(Optional.of(workoutType));
        when(workoutTypeMapper.toDto(workoutType)).thenReturn(dto);

        WorkoutTypeDto result = workoutTypeService.getWorkoutTypeByName(name);

        assertThat(result).isNotNull();
    }

    @Test
    void getWorkoutTypeByName_ShouldThrowException_WhenNotFound() {
        String name = "Несуществующий";

        when(workoutTypeRepository.findByNameIgnoreCase(name)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutTypeService.getWorkoutTypeByName(name))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void createWorkoutType_ShouldReturnWorkoutType_WhenValid() {
        WorkoutTypeDto dto = new WorkoutTypeDto();
        dto.setName("Йога");
        dto.setCategory(WorkoutCategory.GROUP);

        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(1L);
        workoutType.setName(dto.getName());

        WorkoutTypeDto responseDto = new WorkoutTypeDto();
        responseDto.setName(dto.getName());

        when(workoutTypeRepository.findByNameIgnoreCase(dto.getName())).thenReturn(Optional.empty());
        when(workoutTypeMapper.toEntity(dto)).thenReturn(workoutType);
        when(workoutTypeRepository.save(any(WorkoutType.class))).thenReturn(workoutType);
        when(workoutTypeMapper.toDto(workoutType)).thenReturn(responseDto);

        WorkoutTypeDto result = workoutTypeService.createWorkoutType(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(dto.getName());
        verify(workoutTypeRepository).save(any(WorkoutType.class));
    }

    @Test
    void createWorkoutType_ShouldThrowException_WhenNameExists() {
        WorkoutTypeDto dto = new WorkoutTypeDto();
        dto.setName("Йога");

        WorkoutType existing = new WorkoutType();
        existing.setName("Йога");

        when(workoutTypeRepository.findByNameIgnoreCase(dto.getName())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> workoutTypeService.createWorkoutType(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");

        verify(workoutTypeRepository, never()).save(any());
    }

    @Test
    void updateWorkoutType_ShouldUpdateWorkoutType_WhenValid() {
        Long id = 1L;
        WorkoutTypeDto dto = new WorkoutTypeDto();
        dto.setName("Новое название");
        dto.setCategory(WorkoutCategory.GROUP);

        WorkoutType existing = new WorkoutType();
        existing.setId(id);
        existing.setName("Старое название");

        WorkoutType updated = new WorkoutType();
        updated.setId(id);
        updated.setName(dto.getName());

        WorkoutTypeDto responseDto = new WorkoutTypeDto();
        responseDto.setName(dto.getName());

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(workoutTypeRepository.findByNameIgnoreCase(dto.getName())).thenReturn(Optional.empty());
        when(workoutTypeRepository.save(any(WorkoutType.class))).thenReturn(updated);
        when(workoutTypeMapper.toDto(updated)).thenReturn(responseDto);

        WorkoutTypeDto result = workoutTypeService.updateWorkoutType(id, dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(dto.getName());
        verify(workoutTypeMapper).updateEntity(dto, existing);
    }

    @Test
    void updateWorkoutType_ShouldThrowException_WhenNotFound() {
        Long id = 999L;
        WorkoutTypeDto dto = new WorkoutTypeDto();

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutTypeService.updateWorkoutType(id, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(workoutTypeRepository, never()).save(any());
    }

    @Test
    void updateWorkoutType_ShouldThrowException_WhenNameConflict() {
        Long id = 1L;
        WorkoutTypeDto dto = new WorkoutTypeDto();
        dto.setName("Занятое название");

        WorkoutType existing = new WorkoutType();
        existing.setId(id);
        existing.setName("Старое название");

        WorkoutType conflict = new WorkoutType();
        conflict.setId(2L);
        conflict.setName("Занятое название");

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(workoutTypeRepository.findByNameIgnoreCase(dto.getName())).thenReturn(Optional.of(conflict));

        assertThatThrownBy(() -> workoutTypeService.updateWorkoutType(id, dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");

        verify(workoutTypeRepository, never()).save(any());
    }

    @Test
    void deactivateWorkoutType_ShouldSetActiveFalse_WhenExists() {
        Long id = 1L;
        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(id);
        workoutType.setIsActive(true);

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.of(workoutType));

        workoutTypeService.deactivateWorkoutType(id);

        assertThat(workoutType.getIsActive()).isFalse();
        verify(workoutTypeRepository).save(workoutType);
    }

    @Test
    void deactivateWorkoutType_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutTypeService.deactivateWorkoutType(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(workoutTypeRepository, never()).save(any());
    }

    @Test
    void getActiveWorkoutTypes_ShouldReturnList() {
        WorkoutType workoutType = new WorkoutType();
        workoutType.setIsActive(true);
        WorkoutTypeDto dto = new WorkoutTypeDto();

        when(workoutTypeRepository.findByIsActiveTrue()).thenReturn(List.of(workoutType));
        when(workoutTypeMapper.toDto(workoutType)).thenReturn(dto);

        List<WorkoutTypeDto> result = workoutTypeService.getActiveWorkoutTypes();

        assertThat(result).hasSize(1);
    }

    @Test
    void getWorkoutTypesByCategory_ShouldReturnList() {
        WorkoutCategory category = WorkoutCategory.GROUP;
        WorkoutType workoutType = new WorkoutType();
        workoutType.setCategory(category);
        WorkoutTypeDto dto = new WorkoutTypeDto();

        when(workoutTypeRepository.findByCategory(category)).thenReturn(List.of(workoutType));
        when(workoutTypeMapper.toDto(workoutType)).thenReturn(dto);

        List<WorkoutTypeDto> result = workoutTypeService.getWorkoutTypesByCategory(category);

        assertThat(result).hasSize(1);
    }

    @Test
    void getWorkoutTypesByTrainer_ShouldReturnList() {
        Long trainerId = 1L;
        WorkoutType workoutType = new WorkoutType();
        WorkoutTypeDto dto = new WorkoutTypeDto();

        when(workoutTypeRepository.findByTrainerId(trainerId)).thenReturn(List.of(workoutType));
        when(workoutTypeMapper.toDto(workoutType)).thenReturn(dto);

        List<WorkoutTypeDto> result = workoutTypeService.getWorkoutTypesByTrainer(trainerId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getWorkoutTypesBySubscription_ShouldReturnList() {
        Long subscriptionId = 1L;
        WorkoutType workoutType = new WorkoutType();
        WorkoutTypeDto dto = new WorkoutTypeDto();

        when(workoutTypeRepository.findBySubscriptionId(subscriptionId)).thenReturn(List.of(workoutType));
        when(workoutTypeMapper.toDto(workoutType)).thenReturn(dto);

        List<WorkoutTypeDto> result = workoutTypeService.getWorkoutTypesBySubscription(subscriptionId);

        assertThat(result).hasSize(1);
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenExists() {
        String name = "Йога";

        when(workoutTypeRepository.existsByNameIgnoreCase(name)).thenReturn(true);

        boolean result = workoutTypeService.existsByName(name);

        assertThat(result).isTrue();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNotExists() {
        String name = "Несуществующий";

        when(workoutTypeRepository.existsByNameIgnoreCase(name)).thenReturn(false);

        boolean result = workoutTypeService.existsByName(name);

        assertThat(result).isFalse();
    }

    @Test
    void deleteWorkoutType_ShouldDeleteWorkoutType_WhenNoDependencies() {
        Long id = 1L;
        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(id);
        workoutType.setTrainers(new ArrayList<>());
        workoutType.setSubscriptions(new ArrayList<>());
        workoutType.setWorkoutSessions(new ArrayList<>());

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.of(workoutType));

        workoutTypeService.deleteWorkoutType(id);

        verify(workoutTypeRepository).delete(workoutType);
    }

    @Test
    void deleteWorkoutType_ShouldThrowException_WhenHasTrainers() {
        Long id = 1L;
        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(id);
        workoutType.setTrainers(List.of(new Trainer()));
        workoutType.setSubscriptions(new ArrayList<>());
        workoutType.setWorkoutSessions(new ArrayList<>());

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.of(workoutType));

        assertThatThrownBy(() -> workoutTypeService.deleteWorkoutType(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Невозможно удалить");

        verify(workoutTypeRepository, never()).delete(any());
    }

    @Test
    void deleteWorkoutType_ShouldThrowException_WhenHasSubscriptions() {
        Long id = 1L;
        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(id);
        workoutType.setTrainers(new ArrayList<>());
        workoutType.setSubscriptions(List.of(new Subscription()));
        workoutType.setWorkoutSessions(new ArrayList<>());

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.of(workoutType));

        assertThatThrownBy(() -> workoutTypeService.deleteWorkoutType(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Невозможно удалить");

        verify(workoutTypeRepository, never()).delete(any());
    }

    @Test
    void deleteWorkoutType_ShouldThrowException_WhenHasWorkoutSessions() {
        Long id = 1L;
        WorkoutType workoutType = new WorkoutType();
        workoutType.setId(id);
        workoutType.setTrainers(new ArrayList<>());
        workoutType.setSubscriptions(new ArrayList<>());
        workoutType.setWorkoutSessions(List.of(new WorkoutSession()));

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.of(workoutType));

        assertThatThrownBy(() -> workoutTypeService.deleteWorkoutType(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Невозможно удалить");

        verify(workoutTypeRepository, never()).delete(any());
    }

    @Test
    void deleteWorkoutType_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(workoutTypeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutTypeService.deleteWorkoutType(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(workoutTypeRepository, never()).delete(any());
    }
}
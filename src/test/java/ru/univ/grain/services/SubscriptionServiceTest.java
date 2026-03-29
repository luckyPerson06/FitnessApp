package ru.univ.grain.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.*;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.SubscriptionMapper;
import ru.univ.grain.repositories.SubscriptionRepository;
import ru.univ.grain.repositories.VisitRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private WorkoutTypeRepository workoutTypeRepository;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService.init();
    }

    @Test
    void createSubscription_ShouldReturnSubscription_WhenValid() {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Премиум")
                .price(BigDecimal.valueOf(5000))
                .subscriptionType(SubscriptionType.UNLIMITED)
                .durationDays(30)
                .build();

        Subscription subscription = Subscription.builder()
                .id(1L)
                .name(dto.getName())
                .build();

        SubscriptionDto responseDto = SubscriptionDto.builder()
                .name(dto.getName())
                .build();

        when(subscriptionRepository.findByName(dto.getName())).thenReturn(Optional.empty());
        when(subscriptionMapper.toEntity(dto)).thenReturn(subscription);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionMapper.toDto(subscription)).thenReturn(responseDto);

        SubscriptionDto result = subscriptionService.createSubscription(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(dto.getName());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void createSubscription_ShouldThrowException_WhenNameExists() {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Базовый")
                .build();

        Subscription existing = Subscription.builder()
                .name("Базовый")
                .build();

        when(subscriptionRepository.findByName(dto.getName())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> subscriptionService.createSubscription(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void getSubscriptionById_ShouldReturnSubscription_WhenExists() {
        Long id = 1L;
        Subscription subscription = Subscription.builder()
                .id(id)
                .name("Базовый")
                .build();

        SubscriptionDto responseDto = SubscriptionDto.builder()
                .name("Базовый")
                .build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toDto(subscription)).thenReturn(responseDto);

        SubscriptionDto result = subscriptionService.getSubscriptionById(id);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Базовый");
    }

    @Test
    void getSubscriptionById_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void updateSubscription_ShouldUpdateSubscription_WhenValid() {
        Long id = 1L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Новое название")
                .price(BigDecimal.valueOf(6000))
                .subscriptionType(SubscriptionType.UNLIMITED)
                .durationDays(60)
                .build();

        Subscription existing = Subscription.builder()
                .id(id)
                .name("Старое название")
                .build();

        Subscription updated = Subscription.builder()
                .id(id)
                .name(dto.getName())
                .build();

        SubscriptionDto responseDto = SubscriptionDto.builder()
                .name(dto.getName())
                .build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.findByName(dto.getName())).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(updated);
        when(subscriptionMapper.toDto(updated)).thenReturn(responseDto);

        SubscriptionDto result = subscriptionService.updateSubscription(id, dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(dto.getName());
        verify(subscriptionMapper).updateEntity(dto, existing);
    }

    @Test
    void updateSubscription_ShouldThrowException_WhenNameConflict() {
        Long id = 1L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Занятое название")
                .build();

        Subscription existing = Subscription.builder()
                .id(id)
                .name("Старое название")
                .build();

        Subscription conflict = Subscription.builder()
                .id(2L)
                .name("Занятое название")
                .build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.findByName(dto.getName())).thenReturn(Optional.of(conflict));

        assertThatThrownBy(() -> subscriptionService.updateSubscription(id, dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("уже существует");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void deleteSubscription_ShouldDeleteSubscription_WhenExists() {
        Long id = 1L;
        Subscription subscription = Subscription.builder()
                .id(id)
                .build();

        List<Visit> visits = new ArrayList<>();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
        when(visitRepository.findBySubscriptionId(id)).thenReturn(visits);

        subscriptionService.deleteSubscription(id);

        verify(visitRepository).findBySubscriptionId(id);
        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    void deleteSubscription_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.deleteSubscription(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(subscriptionRepository, never()).delete(any());
    }

    @Test
    void expireSubscription_ShouldChangeStatus_WhenExists() {
        Long id = 1L;
        Subscription subscription = Subscription.builder()
                .id(id)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));

        subscriptionService.expireSubscription(id);

        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void expireSubscription_ShouldThrowException_WhenNotFound() {
        Long id = 999L;

        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.expireSubscription(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void addWorkoutType_ShouldAdd_WhenValid() {
        Long subscriptionId = 1L;
        Long workoutTypeId = 1L;

        Subscription subscription = Subscription.builder()
                .id(subscriptionId)
                .allowedWorkoutTypes(new ArrayList<>())
                .build();

        WorkoutType workoutType = WorkoutType.builder()
                .id(workoutTypeId)
                .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(workoutTypeRepository.findById(workoutTypeId)).thenReturn(Optional.of(workoutType));

        subscriptionService.addWorkoutType(subscriptionId, workoutTypeId);

        assertThat(subscription.getAllowedWorkoutTypes()).contains(workoutType);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void addWorkoutType_ShouldThrowException_WhenSubscriptionNotFound() {
        Long subscriptionId = 999L;
        Long workoutTypeId = 1L;

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.addWorkoutType(subscriptionId, workoutTypeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void removeWorkoutType_ShouldRemove_WhenExists() {
        Long subscriptionId = 1L;
        Long workoutTypeId = 1L;

        WorkoutType workoutType = WorkoutType.builder()
                .id(workoutTypeId)
                .build();

        List<WorkoutType> allowed = new ArrayList<>();
        allowed.add(workoutType);

        Subscription subscription = Subscription.builder()
                .id(subscriptionId)
                .allowedWorkoutTypes(allowed)
                .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        subscriptionService.removeWorkoutType(subscriptionId, workoutTypeId);

        assertThat(subscription.getAllowedWorkoutTypes()).doesNotContain(workoutType);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void getActiveSubscriptions_ShouldReturnList() {
        Subscription sub1 = Subscription.builder()
                .status(SubscriptionStatus.ACTIVE)
                .build();
        Subscription sub2 = Subscription.builder()
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of(sub1, sub2));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());
        when(subscriptionMapper.toDto(sub2)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getActiveSubscriptions();

        assertThat(result).hasSize(2);
    }

    @Test
    void getSubscriptionsByType_ShouldReturnFilteredList() {
        Subscription sub1 = Subscription.builder()
                .subscriptionType(SubscriptionType.UNLIMITED)
                .build();
        Subscription sub2 = Subscription.builder()
                .subscriptionType(SubscriptionType.UNLIMITED)
                .build();

        when(subscriptionRepository.findBySubscriptionType(SubscriptionType.UNLIMITED)).thenReturn(List.of(sub1, sub2));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());
        when(subscriptionMapper.toDto(sub2)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByType(SubscriptionType.UNLIMITED);

        assertThat(result).hasSize(2);
    }

    @Test
    void getSubscriptionByName_ShouldReturnSubscription_WhenExists() {
        String name = "Базовый";
        Subscription subscription = Subscription.builder()
                .id(1L)
                .name(name)
                .build();

        SubscriptionDto responseDto = SubscriptionDto.builder()
                .name(name)
                .build();

        when(subscriptionRepository.findByName(name)).thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toDto(subscription)).thenReturn(responseDto);

        SubscriptionDto result = subscriptionService.getSubscriptionByName(name);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    void getSubscriptionByName_ShouldThrowException_WhenNotFound() {
        String name = "Несуществующий";

        when(subscriptionRepository.findByName(name)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionByName(name))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getSubscriptionsByStatus_ShouldReturnList() {
        SubscriptionStatus status = SubscriptionStatus.EXPIRED;
        Subscription sub1 = Subscription.builder()
                .status(status)
                .build();
        Subscription sub2 = Subscription.builder()
                .status(status)
                .build();

        when(subscriptionRepository.findByStatus(status)).thenReturn(List.of(sub1, sub2));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());
        when(subscriptionMapper.toDto(sub2)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByStatus(status);

        assertThat(result).hasSize(2);
    }

    @Test
    void getSubscriptionsByStatus_ShouldReturnEmptyList_WhenNoSubscriptions() {
        SubscriptionStatus status = SubscriptionStatus.CANCELLED;

        when(subscriptionRepository.findByStatus(status)).thenReturn(List.of());

        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByStatus(status);

        assertThat(result).isEmpty();
    }

    @Test
    void getSubscriptionsByType_ShouldReturnEmptyList_WhenNoSubscriptions() {
        SubscriptionType type = SubscriptionType.LIMITED;

        when(subscriptionRepository.findBySubscriptionType(type)).thenReturn(List.of());

        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByType(type);

        assertThat(result).isEmpty();
    }

    @Test
    void getSubscriptionsByWorkoutType_ShouldReturnList() {
        Long workoutTypeId = 1L;
        Subscription sub1 = Subscription.builder()
                .id(1L)
                .build();
        Subscription sub2 = Subscription.builder()
                .id(2L)
                .build();

        when(subscriptionRepository.findByAllowedWorkoutTypeId(workoutTypeId)).thenReturn(List.of(sub1, sub2));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());
        when(subscriptionMapper.toDto(sub2)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByWorkoutType(workoutTypeId);

        assertThat(result).hasSize(2);
    }

    @Test
    void getSubscriptionsByWorkoutType_ShouldReturnEmptyList_WhenNoSubscriptions() {
        Long workoutTypeId = 999L;

        when(subscriptionRepository.findByAllowedWorkoutTypeId(workoutTypeId)).thenReturn(List.of());

        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByWorkoutType(workoutTypeId);

        assertThat(result).isEmpty();
    }

    @Test
    void getExpiredSubscriptions_ShouldReturnList() {
        Subscription sub1 = Subscription.builder()
                .status(SubscriptionStatus.EXPIRED)
                .build();

        when(subscriptionRepository.findByStatus(SubscriptionStatus.EXPIRED)).thenReturn(List.of(sub1));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getExpiredSubscriptions();

        assertThat(result).hasSize(1);
    }

    @Test
    void getCancelledSubscriptions_ShouldReturnList() {
        Subscription sub1 = Subscription.builder()
                .status(SubscriptionStatus.CANCELLED)
                .build();

        when(subscriptionRepository.findByStatus(SubscriptionStatus.CANCELLED)).thenReturn(List.of(sub1));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getCancelledSubscriptions();

        assertThat(result).hasSize(1);
    }

    @Test
    void getUsedSubscriptions_ShouldReturnList() {
        Subscription sub1 = Subscription.builder()
                .status(SubscriptionStatus.USED)
                .build();

        when(subscriptionRepository.findByStatus(SubscriptionStatus.USED)).thenReturn(List.of(sub1));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getUsedSubscriptions();

        assertThat(result).hasSize(1);
    }

    @Test
    void addWorkoutType_ShouldNotAdd_WhenAlreadyExists() {
        Long subscriptionId = 1L;
        Long workoutTypeId = 1L;

        WorkoutType workoutType = WorkoutType.builder()
                .id(workoutTypeId)
                .build();

        List<WorkoutType> allowed = new ArrayList<>();
        allowed.add(workoutType);

        Subscription subscription = Subscription.builder()
                .id(subscriptionId)
                .allowedWorkoutTypes(allowed)
                .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(workoutTypeRepository.findById(workoutTypeId)).thenReturn(Optional.of(workoutType));

        subscriptionService.addWorkoutType(subscriptionId, workoutTypeId);

        assertThat(subscription.getAllowedWorkoutTypes()).hasSize(1);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void addWorkoutType_ShouldThrowException_WhenWorkoutTypeNotFound() {
        Long subscriptionId = 1L;
        Long workoutTypeId = 999L;

        Subscription subscription = Subscription.builder()
                .id(subscriptionId)
                .allowedWorkoutTypes(new ArrayList<>())
                .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(workoutTypeRepository.findById(workoutTypeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.addWorkoutType(subscriptionId, workoutTypeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void removeWorkoutType_ShouldThrowException_WhenWorkoutTypeNotFound() {
        Long subscriptionId = 1L;
        Long workoutTypeId = 999L;

        Subscription subscription = Subscription.builder()
                .id(subscriptionId)
                .allowedWorkoutTypes(new ArrayList<>())
                .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> subscriptionService.removeWorkoutType(subscriptionId, workoutTypeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void deleteSubscription_ShouldSetSubscriptionNullOnVisits() {
        Long id = 1L;
        Subscription subscription = Subscription.builder()
                .id(id)
                .build();

        Visit visit1 = new Visit();
        Visit visit2 = new Visit();
        List<Visit> visits = List.of(visit1, visit2);

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
        when(visitRepository.findBySubscriptionId(id)).thenReturn(visits);

        subscriptionService.deleteSubscription(id);

        verify(visitRepository).findBySubscriptionId(id);
        verify(subscriptionRepository).delete(subscription);
        assertThat(visit1.getSubscription()).isNull();
        assertThat(visit2.getSubscription()).isNull();
    }

    @Test
    void updateSubscription_ShouldUpdate_WhenNameNotChanged() {
        Long id = 1L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Старое название")
                .price(BigDecimal.valueOf(7000))
                .subscriptionType(SubscriptionType.UNLIMITED)
                .durationDays(90)
                .build();

        Subscription existing = Subscription.builder()
                .id(id)
                .name("Старое название")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription updated = Subscription.builder()
                .id(id)
                .name("Старое название")
                .price(BigDecimal.valueOf(7000))
                .build();

        SubscriptionDto responseDto = SubscriptionDto.builder()
                .name("Старое название")
                .price(BigDecimal.valueOf(7000))
                .build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(updated);
        when(subscriptionMapper.toDto(updated)).thenReturn(responseDto);

        SubscriptionDto result = subscriptionService.updateSubscription(id, dto);

        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(7000));
        verify(subscriptionMapper).updateEntity(dto, existing);
        verify(subscriptionRepository).save(existing);
    }

    @Test
    void getSubscriptionsByType_ShouldReturnEmptyList_WhenTypeIsNull() {
        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByType(null);

        assertThat(result).isEmpty();
    }

    @Test
    void getSubscriptionsByStatus_ShouldReturnEmptyList_WhenStatusIsNull() {
        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByStatus(null);

        assertThat(result).isEmpty();
    }

    @Test
    void getSubscriptionsByWorkoutType_ShouldReturnEmptyList_WhenWorkoutTypeIdIsNull() {
        List<SubscriptionDto> result = subscriptionService.getSubscriptionsByWorkoutType(null);

        assertThat(result).isEmpty();
    }

    @Test
    void getActiveSubscriptions_ShouldReturnEmptyList_WhenNoActive() {
        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of());

        List<SubscriptionDto> result = subscriptionService.getActiveSubscriptions();

        assertThat(result).isEmpty();
    }

    @Test
    void getExpiredSubscriptions_ShouldReturnEmptyList_WhenNoExpired() {
        when(subscriptionRepository.findByStatus(SubscriptionStatus.EXPIRED)).thenReturn(List.of());

        List<SubscriptionDto> result = subscriptionService.getExpiredSubscriptions();

        assertThat(result).isEmpty();
    }

    @Test
    void getCancelledSubscriptions_ShouldReturnEmptyList_WhenNoCancelled() {
        when(subscriptionRepository.findByStatus(SubscriptionStatus.CANCELLED)).thenReturn(List.of());

        List<SubscriptionDto> result = subscriptionService.getCancelledSubscriptions();

        assertThat(result).isEmpty();
    }

    @Test
    void getUsedSubscriptions_ShouldReturnEmptyList_WhenNoUsed() {
        when(subscriptionRepository.findByStatus(SubscriptionStatus.USED)).thenReturn(List.of());

        List<SubscriptionDto> result = subscriptionService.getUsedSubscriptions();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllSubscriptions_ShouldReturnEmptyList_WhenNoSubscriptions() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        List<SubscriptionDto> result = subscriptionService.getAllSubscriptions();

        assertThat(result).isEmpty();
    }

    @Test
    void updateSubscription_ShouldNotCheckName_WhenNameEqualsIgnoreCase() {
        Long id = 1L;
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Старое название")
                .price(BigDecimal.valueOf(7000))
                .subscriptionType(SubscriptionType.UNLIMITED)
                .durationDays(90)
                .build();

        Subscription existing = Subscription.builder()
                .id(id)
                .name("Старое название")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription updated = Subscription.builder()
                .id(id)
                .name("Старое название")
                .price(BigDecimal.valueOf(7000))
                .build();

        SubscriptionDto responseDto = SubscriptionDto.builder()
                .name("Старое название")
                .price(BigDecimal.valueOf(7000))
                .build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(updated);
        when(subscriptionMapper.toDto(updated)).thenReturn(responseDto);

        SubscriptionDto result = subscriptionService.updateSubscription(id, dto);

        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(7000));
        verify(subscriptionMapper).updateEntity(dto, existing);
        verify(subscriptionRepository).save(existing);
        verify(subscriptionRepository, never()).findByName(anyString());
    }

    @Test
    void getAllSubscriptions_ShouldReturnList_WhenSubscriptionsExist() {
        Subscription sub1 = Subscription.builder()
                .id(1L)
                .name("Базовый")
                .build();
        Subscription sub2 = Subscription.builder()
                .id(2L)
                .name("Премиум")
                .build();

        SubscriptionDto dto1 = SubscriptionDto.builder()
                .name("Базовый")
                .build();
        SubscriptionDto dto2 = SubscriptionDto.builder()
                .name("Премиум")
                .build();

        when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));
        when(subscriptionMapper.toDto(sub1)).thenReturn(dto1);
        when(subscriptionMapper.toDto(sub2)).thenReturn(dto2);

        List<SubscriptionDto> result = subscriptionService.getAllSubscriptions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Базовый");
        assertThat(result.get(1).getName()).isEqualTo("Премиум");
    }

    @Test
    void getActiveSubscriptions_ShouldCallSelfInjection() {
        Subscription sub1 = Subscription.builder()
                .status(SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of(sub1));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getActiveSubscriptions();

        assertThat(result).hasSize(1);
        verify(subscriptionRepository).findByStatus(SubscriptionStatus.ACTIVE);
    }

    @Test
    void getExpiredSubscriptions_ShouldCallSelfInjection() {
        Subscription sub1 = Subscription.builder()
                .status(SubscriptionStatus.EXPIRED)
                .build();

        when(subscriptionRepository.findByStatus(SubscriptionStatus.EXPIRED)).thenReturn(List.of(sub1));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getExpiredSubscriptions();

        assertThat(result).hasSize(1);
        verify(subscriptionRepository).findByStatus(SubscriptionStatus.EXPIRED);
    }

    @Test
    void getCancelledSubscriptions_ShouldCallSelfInjection() {
        Subscription sub1 = Subscription.builder()
                .status(SubscriptionStatus.CANCELLED)
                .build();

        when(subscriptionRepository.findByStatus(SubscriptionStatus.CANCELLED)).thenReturn(List.of(sub1));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getCancelledSubscriptions();

        assertThat(result).hasSize(1);
        verify(subscriptionRepository).findByStatus(SubscriptionStatus.CANCELLED);
    }

    @Test
    void getUsedSubscriptions_ShouldCallSelfInjection() {
        Subscription sub1 = Subscription.builder()
                .status(SubscriptionStatus.USED)
                .build();

        when(subscriptionRepository.findByStatus(SubscriptionStatus.USED)).thenReturn(List.of(sub1));
        when(subscriptionMapper.toDto(sub1)).thenReturn(SubscriptionDto.builder().build());

        List<SubscriptionDto> result = subscriptionService.getUsedSubscriptions();

        assertThat(result).hasSize(1);
        verify(subscriptionRepository).findByStatus(SubscriptionStatus.USED);
    }


    @Test
    void updateSubscription_ShouldThrowException_WhenNotFound() {
        Long id = 999L;
        SubscriptionDto dto = SubscriptionDto.builder().name("Тест").build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.updateSubscription(id, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(subscriptionRepository, never()).save(any());
    }



    @Test
    void removeWorkoutType_ShouldThrowException_WhenSubscriptionNotFound() {
        Long subscriptionId = 999L;
        Long workoutTypeId = 1L;

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.removeWorkoutType(subscriptionId, workoutTypeId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(subscriptionRepository, never()).save(any());
    }

}
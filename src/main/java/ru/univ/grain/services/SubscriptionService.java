package ru.univ.grain.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.entities.*;
import ru.univ.grain.repositories.SubscriptionRepository;
import ru.univ.grain.repositories.VisitRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.mapper.SubscriptionMapper;
import ru.univ.grain.exception.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final VisitRepository visitRepository;

    private SubscriptionService self;


    @PostConstruct
    public void init() {
        this.self = this;
    }



    private static final String SUBSCRIPTION_NOT_FOUND = "Абонемент с id %d не найден";
    private static final String SUBSCRIPTION_NAME_EXISTS = "Абонемент с названием '%s' уже существует";
    private static final String WORKOUT_TYPE_NOT_FOUND = "Тип тренировки с id %d не найден";
    private static final String INVALID_SUBSCRIPTION = "Некорректные параметры абонемента";

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionById(final Long id) {
        final Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, id)));
        return subscriptionMapper.toDto(subscription);
    }

    @Transactional
    public SubscriptionDto createSubscription(final SubscriptionDto dto) {
        subscriptionRepository.findByName(dto.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            String.format(SUBSCRIPTION_NAME_EXISTS, dto.getName()));
                });

        if (isInvalidSubscription(dto)) {
            throw new BusinessException(INVALID_SUBSCRIPTION);
        }

        final Subscription subscription = subscriptionMapper.toEntity(dto);
        final Subscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(saved);
    }

    @Transactional
    public SubscriptionDto updateSubscription(final Long id, final SubscriptionDto dto) {
        final Subscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, id)));

        if (!existing.getName().equals(dto.getName())) {
            subscriptionRepository.findByName(dto.getName())
                    .ifPresent(subscriptionWithSameName -> {
                        throw new DuplicateResourceException(
                                String.format(SUBSCRIPTION_NAME_EXISTS, dto.getName()));
                    });
        }

        if (isInvalidSubscription(dto)) {
            throw new BusinessException(INVALID_SUBSCRIPTION);
        }

        subscriptionMapper.updateEntity(dto, existing);
        final Subscription updated = subscriptionRepository.save(existing);
        return subscriptionMapper.toDto(updated);
    }

    @Transactional
    public void deleteSubscription(Long id) {
        final Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, id)));

        final List<Visit> visits = visitRepository.findBySubscriptionId(id);

        for (Visit visit : visits) {
            visit.setSubscription(null);
        }

        subscriptionRepository.delete(subscription);
    }

    @Transactional
    public void expireSubscription(final Long id) {
        final Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, id)));

        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void addWorkoutType(final Long subscriptionId, final Long workoutTypeId) {
        final Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, subscriptionId)));

        final WorkoutType workoutType = workoutTypeRepository.findById(workoutTypeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(WORKOUT_TYPE_NOT_FOUND, workoutTypeId)));

        if (!subscription.getAllowedWorkoutTypes().contains(workoutType)) {
            subscription.getAllowedWorkoutTypes().add(workoutType);
            subscriptionRepository.save(subscription);
        }
    }

    @Transactional
    public void removeWorkoutType(final Long subscriptionId, final Long workoutTypeId) {
        final Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, subscriptionId)));

        final boolean removed = subscription.getAllowedWorkoutTypes().removeIf(
                wt -> wt.getId().equals(workoutTypeId));

        if (!removed) {
            throw new ResourceNotFoundException(
                    String.format(WORKOUT_TYPE_NOT_FOUND, workoutTypeId));
        }

        subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getSubscriptionsByType(final SubscriptionType type) {
        return subscriptionRepository.findBySubscriptionType(type).stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getSubscriptionsByWorkoutType(final Long workoutTypeId) {
        return subscriptionRepository.findByAllowedWorkoutTypeId(workoutTypeId).stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getSubscriptionsByStatus(final SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status).stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getActiveSubscriptions() {
        return self.getSubscriptionsByStatus(SubscriptionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getExpiredSubscriptions() {
        return self.getSubscriptionsByStatus(SubscriptionStatus.EXPIRED);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getCancelledSubscriptions() {
        return self.getSubscriptionsByStatus(SubscriptionStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getUsedSubscriptions() {
        return self.getSubscriptionsByStatus(SubscriptionStatus.USED);
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionByName(final String name) {
        return subscriptionRepository.findByName(name)
                .map(subscriptionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Абонемент с названием '" + name + "' не найден"));
    }

    private boolean isInvalidSubscription(final SubscriptionDto dto) {
        if (dto.getSubscriptionType() == SubscriptionType.LIMITED) {
            return dto.getMaxVisits() == null || dto.getMaxVisits() <= 0;
        } else if (dto.getSubscriptionType() == SubscriptionType.UNLIMITED) {
            return dto.getMaxVisits() != null;
        }
        return false;
    }
}

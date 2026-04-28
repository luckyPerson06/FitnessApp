package ru.univ.grain.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.cache.AppCache;
import ru.univ.grain.cache.CacheKey;
import ru.univ.grain.cache.CacheRegion;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.Subscription;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;
import ru.univ.grain.entities.WorkoutType;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.SubscriptionMapper;
import ru.univ.grain.repositories.SubscriptionRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final AppCache appCache;

    private static final String SUBSCRIPTION_NOT_FOUND = "Абонемент с id %d не найден";
    private static final String SUBSCRIPTION_NAME_EXISTS = "Абонемент с названием '%s' уже существует";
    private static final String WORKOUT_TYPE_NOT_FOUND = "Тип тренировки с id %d не найден";

    private SubscriptionService self;

    @PostConstruct
    public void init() {
        this.self = this;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getAllSubscriptions() {
        final CacheKey key = CacheKey.forSubscriptions();

        final List<SubscriptionDto> cached = appCache.get(key);
        if (cached != null) {
            return cached;
        }

        final List<SubscriptionDto> result = subscriptionRepository.findAll().stream()
                .map(subscriptionMapper::toDto)
                .toList();

        appCache.put(key, result);
        return result;
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionById(final Long id) {
        final Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, id)));
        return subscriptionMapper.toDto(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionByName(final String name) {
        final Subscription subscription = subscriptionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Абонемент с названием '" + name + "' не найден"));
        return subscriptionMapper.toDto(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getSubscriptionsByType(final SubscriptionType type) {
        return subscriptionRepository.findBySubscriptionType(type).stream()
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


    @Transactional
    public SubscriptionDto createSubscription(final SubscriptionDto dto) {
        subscriptionRepository.findByName(dto.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            String.format(SUBSCRIPTION_NAME_EXISTS, dto.getName()));
                });

        final Subscription subscription = subscriptionMapper.toEntity(dto);
        setWorkoutTypes(subscription, dto.getWorkoutTypeIds());
        final Subscription saved = subscriptionRepository.save(subscription);

        appCache.clearRegion(CacheRegion.SUBSCRIPTIONS);
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

        subscriptionMapper.updateEntity(dto, existing);
        setWorkoutTypes(existing, dto.getWorkoutTypeIds());
        final Subscription updated = subscriptionRepository.save(existing);

        appCache.clearRegion(CacheRegion.SUBSCRIPTIONS);
        return subscriptionMapper.toDto(updated);
    }

    @Transactional
    public void deleteSubscription(final Long id) {
        final Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, id)));

        subscriptionRepository.delete(subscription);
        appCache.clearRegion(CacheRegion.SUBSCRIPTIONS);
    }

    @Transactional
    public void expireSubscription(final Long id) {
        final Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(SUBSCRIPTION_NOT_FOUND, id)));

        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(subscription);
        appCache.clearRegion(CacheRegion.SUBSCRIPTIONS);
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
            appCache.clearRegion(CacheRegion.SUBSCRIPTIONS);
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
        appCache.clearRegion(CacheRegion.SUBSCRIPTIONS);
    }

    private void setWorkoutTypes(Subscription subscription, List<Long> workoutTypeIds) {
        if (subscription.getAllowedWorkoutTypes() == null) {
            subscription.setAllowedWorkoutTypes(new ArrayList<>());
        }
        for (WorkoutType wt : subscription.getAllowedWorkoutTypes()) {
            wt.getSubscriptions().remove(subscription);
        }
        subscription.getAllowedWorkoutTypes().clear();
        if (workoutTypeIds != null && !workoutTypeIds.isEmpty()) {
            final List<WorkoutType> types = workoutTypeRepository.findAllById(workoutTypeIds);
            for (WorkoutType wt : types) {
                wt.getSubscriptions().add(subscription);
            }
            subscription.getAllowedWorkoutTypes().addAll(types);
        }
    }

}

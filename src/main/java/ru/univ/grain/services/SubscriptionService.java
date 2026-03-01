package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.entities.Subscription;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;
import ru.univ.grain.entities.WorkoutType;
import ru.univ.grain.repositories.SubscriptionRepository;
import ru.univ.grain.repositories.WorkoutTypeRepository;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.mapper.SubscriptionMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WorkoutTypeRepository workoutTypeRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionById(final Long id) {
        final Subscription subscription = subscriptionRepository.findById(id).orElse(null);
        if (subscription == null) {
            return null;
        }
        return subscriptionMapper.toDto(subscription);
    }

    @Transactional
    public SubscriptionDto createSubscription(final SubscriptionDto dto) {
        final Subscription existing = subscriptionRepository.findByName(dto.getName());
        if (existing != null) {
            return null;
        }

        if (isInvalidSubscription(dto)) {
            return null;
        }

        final Subscription subscription = subscriptionMapper.toEntity(dto);
        final Subscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(saved);
    }

    @Transactional
    public SubscriptionDto updateSubscription(final Long id, final SubscriptionDto dto) {
        final Subscription existing = subscriptionRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        if (!existing.getName().equals(dto.getName())) {
            final Subscription subscriptionWithSameName = subscriptionRepository.findByName(dto.getName());
            if (subscriptionWithSameName != null) {
                return null;
            }
        }

        if (isInvalidSubscription(dto)) {
            return null;
        }

        subscriptionMapper.updateEntity(dto, existing);
        final Subscription updated = subscriptionRepository.save(existing);
        return subscriptionMapper.toDto(updated);
    }

    @Transactional
    public boolean deleteSubscription(final Long id) {
        if (!subscriptionRepository.existsById(id)) {
            return false;
        }
        subscriptionRepository.deleteById(id);
        return true;
    }

    @Transactional
    public boolean expireSubscription(final Long id) {
        final Subscription subscription = subscriptionRepository.findById(id).orElse(null);
        if (subscription == null) {
            return false;
        }
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(subscription);
        return true;
    }

    @Transactional
    public boolean addWorkoutType(final Long subscriptionId, final Long workoutTypeId) {
        final Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);
        final WorkoutType workoutType = workoutTypeRepository.findById(workoutTypeId).orElse(null);

        if (subscription == null || workoutType == null) {
            return false;
        }

        if (!subscription.getAllowedWorkoutTypes().contains(workoutType)) {
            subscription.getAllowedWorkoutTypes().add(workoutType);
            subscriptionRepository.save(subscription);
        }
        return true;
    }

    @Transactional
    public boolean removeWorkoutType(final Long subscriptionId, final Long workoutTypeId) {
        final Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);
        if (subscription == null) {
            return false;
        }

        final boolean removed = subscription.getAllowedWorkoutTypes().removeIf(
                wt -> wt.getId().equals(workoutTypeId));
        if (removed) {
            subscriptionRepository.save(subscription);
        }
        return removed;
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

    private List<SubscriptionDto> getSubscriptionsByStatusInternal(final SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status).stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getSubscriptionsByStatus(final SubscriptionStatus status) {
        return getSubscriptionsByStatusInternal(status);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getActiveSubscriptions() {
        return getSubscriptionsByStatusInternal(SubscriptionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getExpiredSubscriptions() {
        return getSubscriptionsByStatusInternal(SubscriptionStatus.EXPIRED);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getCancelledSubscriptions() {
        return getSubscriptionsByStatusInternal(SubscriptionStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getUsedSubscriptions() {
        return getSubscriptionsByStatusInternal(SubscriptionStatus.USED);
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionByName(final String name) {
        final Subscription subscription = subscriptionRepository.findByName(name);
        return subscription != null ? subscriptionMapper.toDto(subscription) : null;
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

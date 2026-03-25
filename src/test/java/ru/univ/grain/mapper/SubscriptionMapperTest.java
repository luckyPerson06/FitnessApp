package ru.univ.grain.mapper;

import org.junit.jupiter.api.Test;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.entities.Subscription;
import ru.univ.grain.entities.SubscriptionStatus;
import ru.univ.grain.entities.SubscriptionType;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionMapperTest {

    private final SubscriptionMapper subscriptionMapper = new SubscriptionMapperImpl();

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Базовый")
                .description("Базовый абонемент")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .build();

        Subscription result = subscriptionMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Базовый");
        assertThat(result.getDescription()).isEqualTo("Базовый абонемент");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(result.getSubscriptionType()).isEqualTo(SubscriptionType.LIMITED);
        assertThat(result.getMaxVisits()).isEqualTo(8);
        assertThat(result.getDurationDays()).isEqualTo(30);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.getAllowedWorkoutTypes()).isNull();
        assertThat(result.getClients()).isNull();
    }

    @Test
    void toEntity_ShouldMapUnlimitedSubscription() {
        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Премиум")
                .price(BigDecimal.valueOf(5000))
                .subscriptionType(SubscriptionType.UNLIMITED)
                .durationDays(30)
                .build();

        Subscription result = subscriptionMapper.toEntity(dto);

        assertThat(result.getSubscriptionType()).isEqualTo(SubscriptionType.UNLIMITED);
        assertThat(result.getMaxVisits()).isNull();
    }

    @Test
    void toDto_ShouldMapEntityToDto() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .name("Базовый")
                .description("Базовый абонемент")
                .price(BigDecimal.valueOf(3000))
                .subscriptionType(SubscriptionType.LIMITED)
                .maxVisits(8)
                .durationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .allowedWorkoutTypes(new ArrayList<>())
                .clients(new ArrayList<>())
                .build();

        SubscriptionDto result = subscriptionMapper.toDto(subscription);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Базовый");
        assertThat(result.getDescription()).isEqualTo("Базовый абонемент");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(result.getSubscriptionType()).isEqualTo(SubscriptionType.LIMITED);
        assertThat(result.getMaxVisits()).isEqualTo(8);
        assertThat(result.getDurationDays()).isEqualTo(30);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void updateEntity_ShouldUpdateOnlyNonNullFields() {
        Subscription subscription = Subscription.builder()
                .id(1L)
                .name("Старое название")
                .price(BigDecimal.valueOf(2000))
                .build();

        SubscriptionDto dto = SubscriptionDto.builder()
                .name("Новое название")
                .price(BigDecimal.valueOf(3500))
                .build();

        subscriptionMapper.updateEntity(dto, subscription);

        assertThat(subscription.getName()).isEqualTo("Новое название");
        assertThat(subscription.getPrice()).isEqualTo(BigDecimal.valueOf(3500));
    }
}
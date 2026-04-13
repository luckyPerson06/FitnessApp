package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.dto.ProfileResponse;
import ru.univ.grain.dto.SubscriptionDto;
import ru.univ.grain.dto.VisitDto;
import ru.univ.grain.entities.Client;
import ru.univ.grain.entities.User;
import ru.univ.grain.entities.Visit;
import ru.univ.grain.entities.VisitStatus;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.ResourceNotFoundException;
import ru.univ.grain.mapper.SubscriptionMapper;
import ru.univ.grain.mapper.VisitMapper;
import ru.univ.grain.repositories.ClientRepository;
import ru.univ.grain.repositories.VisitRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ClientRepository clientRepository;
    private final VisitRepository visitRepository;
    private final VisitMapper visitMapper;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(final User user) {
        final Client client = getClientFromUser(user);

        final List<SubscriptionDto> activeSubscriptions = client.getSubscriptions().stream()
                .filter(s -> s.getStatus().name().equals("ACTIVE"))
                .map(subscriptionMapper::toDto)
                .toList();

        final List<VisitDto> upcomingVisits = visitRepository.findBookedVisitsByClient(client.getId()).stream()
                .map(visitMapper::toDto)
                .toList();

        final List<VisitDto> visitHistory = visitRepository.findByClientId(client.getId()).stream()
                .filter(v -> v.getStatus() != VisitStatus.BOOKED)
                .map(visitMapper::toDto)
                .toList();

        return ProfileResponse.builder()
                .id(client.getId())
                .fullName(buildFullName(client))
                .email(client.getEmail())
                .phoneNumber(client.getPhoneNumber())
                .status(client.getStatus())
                .activeSubscriptions(activeSubscriptions)
                .upcomingVisits(upcomingVisits)
                .visitHistory(visitHistory)
                .build();
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getUpcomingVisits(final User user) {
        final Client client = getClientFromUser(user);
        return visitRepository.findBookedVisitsByClient(client.getId()).stream()
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getVisitHistory(final User user) {
        final Client client = getClientFromUser(user);
        return visitRepository.findByClientId(client.getId()).stream()
                .filter(v -> v.getStatus() != VisitStatus.BOOKED)
                .map(visitMapper::toDto)
                .toList();
    }

    @Transactional
    public VisitDto cancelVisit(final User user, final Long visitId) {
        final Client client = getClientFromUser(user);
        final Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Запись не найдена"));

        if (!visit.getClient().getId().equals(client.getId())) {
            throw new BusinessException("Вы не можете отменить чужую запись");
        }

        if (visit.getStatus() != VisitStatus.BOOKED) {
            throw new BusinessException("Можно отменить только активную запись");
        }

        visit.setStatus(VisitStatus.CANCELLED);
        return visitMapper.toDto(visitRepository.save(visit));
    }

    private Client getClientFromUser(final User user) {
        final Long clientId = user.getClientId();
        if (clientId == null) {
            throw new ResourceNotFoundException("Клиент не найден");
        }
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден"));
    }

    private String buildFullName(final Client client) {
        final String middleName = client.getMiddleName();
        if (middleName != null && !middleName.isEmpty()) {
            return String.format("%s %s %s",
                    client.getLastName(),
                    client.getFirstName(),
                    middleName);
        }
        return String.format("%s %s", client.getLastName(), client.getFirstName());
    }
}

package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.univ.grain.dto.AuthResponse;
import ru.univ.grain.dto.LoginRequest;
import ru.univ.grain.dto.RegisterRequest;
import ru.univ.grain.entities.Client;
import ru.univ.grain.entities.ClientStatus;
import ru.univ.grain.entities.Role;
import ru.univ.grain.entities.User;
import ru.univ.grain.exception.BusinessException;
import ru.univ.grain.exception.DuplicateResourceException;
import ru.univ.grain.repositories.ClientRepository;
import ru.univ.grain.repositories.UserRepository;
import ru.univ.grain.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(final RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Такой Email уже зарегистрирован");
        }

        final Client client = Client.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .status(ClientStatus.ACTIVE)
                .build();

        final Client savedClient = clientRepository.save(client);

        final User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .isEnabled(true)
                .build();
        user.setClient(savedClient);

        userRepository.save(user);

        final String token = jwtService.generateToken(user);
        final String displayName = buildFullName(savedClient);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .displayName(displayName)
                .clientId(savedClient.getId())
                .build();
    }

    public AuthResponse login(final LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BusinessException("Неверный email или пароль");
        }

        final User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Пользователь не найден"));

        final String token = jwtService.generateToken(user);
        final String displayName = buildDisplayName(user);
        final Long clientId = user.getClientId();

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .displayName(displayName)
                .clientId(clientId)
                .build();
    }

    private String buildDisplayName(final User user) {
        if (user.getRole() == Role.ADMIN) {
            return "Администратор";
        }

        final Long clientId = user.getClientId();
        if (clientId != null) {
            final Client client = clientRepository.findById(clientId).orElse(null);
            if (client != null) {
                return buildFullName(client);
            }
        }

        return user.getEmail();
    }

    private String buildFullName(final Client client) {
        if (client.getMiddleName() != null && !client.getMiddleName().isEmpty()) {
            return String.format("%s %s %s",
                    client.getLastName(),
                    client.getFirstName(),
                    client.getMiddleName());
        }
        return String.format("%s %s", client.getLastName(), client.getFirstName());
    }
}

    package ru.univ.grain.dto;

    import jakarta.validation.constraints.*;
    import lombok.*;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class ClientDto {
        @NotBlank(message = "Имя обязательно")
        private String firstName;

        private String middleName;

        @NotBlank(message = "Фамилия обязательна")
        private String lastName;

        @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Неверный формат телефона")
        private String phoneNumber;

        @Email(message = "Неверный формат email")
        @NotBlank(message = "Email обязателен")
        private String email;

        @NotBlank(message = "Пароль обязателен")
        private String password;
    }

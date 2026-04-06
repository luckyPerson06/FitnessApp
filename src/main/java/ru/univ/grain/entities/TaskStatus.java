package ru.univ.grain.entities;

public enum TaskStatus {
    PENDING("Ожидает выполнения"),
    IN_PROGRESS("Выполняется"),
    COMPLETED("Завершена успешно"),
    FAILED("Завершена с ошибкой");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package ru.univ.grain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.univ.grain.entities.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskInfo {
    private String taskId;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private int progress;
    private int totalItems;
    private int processedItems;
    private List<WorkoutSessionDto> result;
}

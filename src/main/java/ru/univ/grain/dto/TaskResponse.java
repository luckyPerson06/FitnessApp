package ru.univ.grain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String taskId;
    private String message;

    public TaskResponse(String taskId) {
        this.taskId = taskId;
        this.message = "Task created. Use GET /api/async/task/{taskId}/status to check progress.";
    }
}


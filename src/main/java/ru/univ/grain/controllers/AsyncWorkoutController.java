package ru.univ.grain.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.univ.grain.dto.WorkoutSessionBulkRequest;
import ru.univ.grain.dto.TaskInfo;
import ru.univ.grain.dto.TaskResponse;
import ru.univ.grain.entities.TaskStatus;
import ru.univ.grain.services.AsyncWorkoutService;
import ru.univ.grain.storage.TaskStorage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
public class AsyncWorkoutController {

    private final AsyncWorkoutService asyncWorkoutService;
    private final TaskStorage taskStorage;

    @PostMapping("/task")
    public ResponseEntity<TaskResponse> startAsyncProcess(@Valid @RequestBody WorkoutSessionBulkRequest request) {
        final String taskId = UUID.randomUUID().toString();

        final TaskInfo taskInfo = TaskInfo.builder()
                .taskId(taskId)
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalItems(request.getSessions().size())
                .processedItems(0)
                .progress(0)
                .build();

        taskStorage.save(taskId, taskInfo);
        asyncWorkoutService.processWorkoutBulkAsync(request.getSessions(), taskId);

        return ResponseEntity.accepted().body(new TaskResponse(taskId));
    }

    @GetMapping("/task/{taskId}/status")
    public ResponseEntity<TaskInfo> getTaskStatus(@PathVariable String taskId) {
        final TaskInfo taskInfo = taskStorage.get(taskId);
        if (taskInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(taskInfo);
    }

    @GetMapping("/task/{taskId}/result")
    public ResponseEntity<Object> getTaskResult(@PathVariable String taskId) {
        final TaskInfo taskInfo = taskStorage.get(taskId);
        if (taskInfo == null) {
            return ResponseEntity.notFound().build();
        }

        if (taskInfo.getStatus() == TaskStatus.COMPLETED) {
            return ResponseEntity.ok(taskInfo.getResult());
        }

        if (taskInfo.getStatus() == TaskStatus.FAILED) {
            final Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", taskInfo.getErrorMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }

        final Map<String, Object> pendingResponse = new HashMap<>();
        pendingResponse.put("message", "Task not completed yet");
        pendingResponse.put("progress", taskInfo.getProgress());
        pendingResponse.put("status", taskInfo.getStatus());
        pendingResponse.put("processedItems", taskInfo.getProcessedItems());
        pendingResponse.put("totalItems", taskInfo.getTotalItems());
        return ResponseEntity.accepted().body(pendingResponse);
    }

    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        if (!taskStorage.exists(taskId)) {
            return ResponseEntity.notFound().build();
        }
        taskStorage.remove(taskId);
        return ResponseEntity.noContent().build();
    }
}

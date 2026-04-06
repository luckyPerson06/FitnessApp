package ru.univ.grain.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.entities.TaskStatus;
import ru.univ.grain.storage.TaskStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncWorkoutService {

    private final WorkoutSessionService workoutSessionService;
    private final TaskStorage taskStorage;

    @Async("businessExecutor")
    public CompletableFuture<String> processWorkoutBulkAsync(List<WorkoutSessionDto> sessions, String taskId) {
        try {
            Thread.sleep(15000L);

            taskStorage.updateStatus(taskId, TaskStatus.IN_PROGRESS);

            Thread.sleep(10000L);

            processSessions(sessions, taskId);

            return CompletableFuture.completedFuture(taskId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            taskStorage.fail(taskId, "Task was interrupted");
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            taskStorage.fail(taskId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    private void processSessions(List<WorkoutSessionDto> sessions, String taskId) throws InterruptedException {
        final int total = sessions.size();
        int processed = 0;
        final List<WorkoutSessionDto> successfulResults = new ArrayList<>();

        for (WorkoutSessionDto dto : sessions) {
            Thread.sleep(10000L);

            final WorkoutSessionDto result = processSingleSession(dto);
            if (result != null) {
                successfulResults.add(result);
            }
            processed++;
            taskStorage.updateProgress(taskId, processed, total);
        }

        taskStorage.complete(taskId, successfulResults);
    }

    private WorkoutSessionDto processSingleSession(WorkoutSessionDto dto) {
        try {
            return workoutSessionService.validateAndCreateSession(dto);
        } catch (Exception e) {
            return null;
        }
    }
}

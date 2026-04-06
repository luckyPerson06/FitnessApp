package ru.univ.grain.storage;

import org.springframework.stereotype.Component;
import ru.univ.grain.dto.WorkoutSessionDto;
import ru.univ.grain.dto.TaskInfo;
import ru.univ.grain.entities.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaskStorage {

    private final ConcurrentHashMap<String, TaskInfo> tasks = new ConcurrentHashMap<>();

    public void save(String taskId, TaskInfo taskInfo) {
        tasks.put(taskId, taskInfo);
    }

    public TaskInfo get(String taskId) {
        return tasks.get(taskId);
    }

    public void updateStatus(String taskId, TaskStatus status) {
        final TaskInfo task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(status);
            if (status == TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
                task.setStartedAt(LocalDateTime.now());
            }
            if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
    }

    public void updateProgress(String taskId, int processed, int total) {
        final TaskInfo task = tasks.get(taskId);
        if (task != null) {
            task.setProcessedItems(processed);
            task.setTotalItems(total);
            task.setProgress(total > 0 ? (processed * 100 / total) : 0);
        }
    }

    public void complete(String taskId, List<WorkoutSessionDto> result) {
        final TaskInfo task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            task.setResult(result);
            task.setProgress(100);
        }
    }

    public void fail(String taskId, String errorMessage) {
        final TaskInfo task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.FAILED);
            task.setCompletedAt(LocalDateTime.now());
            task.setErrorMessage(errorMessage);
        }
    }

    public boolean exists(String taskId) {
        return tasks.containsKey(taskId);
    }

    public void remove(String taskId) {
        tasks.remove(taskId);
    }
}

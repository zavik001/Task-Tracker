package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Category;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.entity.enums.TaskStatus;
import com.example.demo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> getActiveTasksForUser(User user) {
        return taskRepository.findByUserAndStatusOrderByDeadlineAsc(user, TaskStatus.ACTIVE);
    }

    public List<Task> getAllTasksForUser(User user) {
        return taskRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional
    public boolean completeTask(Long taskId, User user) {
        int updated = taskRepository.markCompletedIfOwned(taskId, user.getId());
        if (updated > 0) {
            log.info("Task {} completed by user {}", taskId, user.getId());
            return true;
        } else {
            log.warn(
                    "Attempt to complete task {} by user {} failed (not owner or already completed)",
                    taskId, user.getId());
            return false;
        }
    }

    @Transactional
    public Task createTask(User user, Category category, String title, LocalDateTime deadline) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (deadline.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }
        Task task = Task.builder().user(user).category(category).title(title.trim())
                .deadline(deadline).status(TaskStatus.ACTIVE).build();
        return save(task);
    }
}

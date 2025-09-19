package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Task;
import com.example.demo.entity.enums.TaskStatus;
import com.example.demo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationService {

    private final TaskRepository taskRepository;
    private final MessageService messageService;

    // Каждую минуту проверяем дедлайны в ближайшие 5 минут
    @Scheduled(fixedRate = 60_000)
    public void sendReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime in5min = now.plusMinutes(5);
            List<Task> upcoming = taskRepository.findByStatusAndDeadlineBetweenAndReminderNotSent(
                    TaskStatus.ACTIVE, now, in5min);
            for (Task task : upcoming) {
                try {
                    messageService.sendMessage(task.getUser().getId(), "Напоминание! Задача \""
                            + task.getTitle() + "\" скоро дедлайн: " + task.getDeadline());
                    markReminderSentSafe(task.getId());
                } catch (Exception e) {
                    log.error("Failed to send reminder for task {}: {}", task.getId(),
                            e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error in sendReminders: {}", e.getMessage(), e);
        }
    }

    @Transactional
    protected void markReminderSentSafe(Long taskId) {
        int updated = taskRepository.markReminderSent(taskId);
        if (updated == 0) {
            log.debug("Reminder already marked or task missing for taskId={}", taskId);
        } else {
            log.info("Marked reminderSent for taskId={}", taskId);
        }
    }
}

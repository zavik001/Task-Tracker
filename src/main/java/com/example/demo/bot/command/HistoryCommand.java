package com.example.demo.bot.command;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import com.example.demo.util.UpdateUtils;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class HistoryCommand implements Command {

    private final TaskService taskService;
    private final UserService userService;
    private final MessageService messageService;

    private static final int PAGE_SIZE = 20;
    private static final int TELEGRAM_MAX = 4000;

    @Override
    public String command() {
        return "/history";
    }

    @Override
    public String description() {
        return "Показать историю всех задач";
    }

    @Override
    public void handle(Update update) {
        User user = UpdateUtils.getUser(userService, update);
        Long chatId = UpdateUtils.getChatId(update);

        List<Task> allTasks = taskService.getAllTasksForUser(user);

        if (allTasks.isEmpty()) {
            messageService.sendMessage(chatId, "У тебя нет задач в истории.");
            return;
        }

        StringBuilder sb = new StringBuilder("Все задачи:\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        int count = 0;
        for (Task task : allTasks) {
            StringBuilder line = new StringBuilder();
            line.append(task.getId()).append(": ").append(task.getTitle()).append(" (")
                    .append(task.getCategory().getName()).append(") - статус: ")
                    .append(task.getStatus()).append(", дедлайн: ")
                    .append(task.getDeadline().format(fmt));
            if (task.getCompletedAt() != null) {
                line.append(", завершено: ").append(task.getCompletedAt().format(fmt));
            }
            line.append("\n");
            if (sb.length() + line.length() > TELEGRAM_MAX || count >= PAGE_SIZE) {
                messageService.sendMessage(chatId, sb.toString());
                sb = new StringBuilder();
                count = 0;
            }
            sb.append(line);
            count++;
        }
        if (sb.length() > 0) {
            messageService.sendMessage(chatId, sb.toString());
        }
    }
}

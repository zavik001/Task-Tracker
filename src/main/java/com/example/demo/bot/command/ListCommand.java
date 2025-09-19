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
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ListCommand implements Command {

    private final TaskService taskService;
    private final UserService userService;
    private final MessageService messageService;

    private static final int TELEGRAM_MAX = 4000;

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "Показать список активных задач";
    }

    @Override
    public void handle(Update update) {
        User user = UpdateUtils.getUser(userService, update);
        Long chatId = UpdateUtils.getChatId(update);
        List<Task> activeTasks = taskService.getActiveTasksForUser(user);

        if (activeTasks.isEmpty()) {
            messageService.sendMessage(chatId, "У тебя нет активных задач.");
            return;
        }

        StringBuilder sb = new StringBuilder("Твои активные задачи:\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();

        for (Task task : activeTasks) {
            String line =
                    task.getId() + ": " + task.getTitle() + " (" + task.getCategory().getName()
                            + ") - дедлайн: " + task.getDeadline().format(fmt) + "\n";
            if (sb.length() + line.length() > TELEGRAM_MAX) {
                messageService.sendMessage(chatId, sb.toString(), inlineKeyboard);
                sb = new StringBuilder();
                inlineKeyboard = new InlineKeyboardMarkup();
            }
            sb.append(line);
            inlineKeyboard.addRow(new InlineKeyboardButton("Выполнить: " + task.getTitle())
                    .callbackData("complete:" + task.getId()));
        }
        if (sb.length() > 0) {
            messageService.sendMessage(chatId, sb.toString(), inlineKeyboard);
        }
    }
}

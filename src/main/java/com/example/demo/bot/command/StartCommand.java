package com.example.demo.bot.command;

import org.springframework.stereotype.Component;
import com.example.demo.entity.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import com.example.demo.util.UpdateUtils;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class StartCommand implements Command {

    private final UserService userService;
    private final MessageService messageService;

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "Начало работы с ботом";
    }

    @Override
    public void handle(Update update) {
        User user = UpdateUtils.getUser(userService, update);
        Long chatId = UpdateUtils.getChatId(update);

        String welcomeMessage = "Привет, "
                + (user.getFirstName() != null ? user.getFirstName() : "друг") + "!\n\n"
                + "Я - твой Task Tracker бот.\n\n" + "Доступные команды:\n"
                + "/add - добавить новую задачу\n" + "/list - показать список активных задач\n"
                + "/history - показать историю задач\n";

        messageService.sendMessage(chatId, welcomeMessage);
    }
}

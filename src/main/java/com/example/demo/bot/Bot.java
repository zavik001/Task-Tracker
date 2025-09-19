package com.example.demo.bot;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.example.demo.bot.command.AddCommand;
import com.example.demo.bot.command.Command;
import com.example.demo.entity.User;
import com.example.demo.service.AddStateService;
import com.example.demo.service.MessageService;
import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class Bot implements UpdatesListener {

    private final TelegramBot telegramBot;
    private final List<Command> commands;
    private final AddCommand addCommand;
    private final AddStateService addStateService;
    private final MessageService messageService;
    private final UserService userService;
    private final TaskService taskService;

    @PostConstruct
    public void init() {
        registerCommands();
        telegramBot.setUpdatesListener(this);
    }

    private void registerCommands() {
        List<BotCommand> botCommands =
                commands.stream().map(cmd -> new BotCommand(cmd.command(), cmd.description()))
                        .collect(Collectors.toList());
        telegramBot.execute(new SetMyCommands(botCommands.toArray(new BotCommand[0])));
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            try {
                if (update.callbackQuery() != null) {
                    handleCallback(update);
                } else if (update.message() != null && update.message().text() != null) {
                    handleMessage(update);
                } else {
                    log.debug("Ignored update: {}", update);
                }
            } catch (Exception e) {
                log.error("Error processing update: {}", e.getMessage(), e);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleMessage(Update update) {
        String text = update.message().text().trim();
        Long telegramId = update.message().from().id();
        Long chatId = update.message().chat().id();
        try {
            if (text.startsWith("/")) {
                addStateService.removeState(telegramId);
                String cmd = text.split(" ")[0];
                commands.stream().filter(c -> c.command().equalsIgnoreCase(cmd)).findFirst()
                        .ifPresentOrElse(c -> c.handle(update),
                                () -> messageService.sendMessage(chatId,
                                        "Неизвестная команда. Используйте доступные команды."));
            } else {
                if (addStateService.getState(telegramId) != null) {
                    addCommand.handle(update);
                } else {
                    messageService.sendMessage(chatId,
                            "Пожалуйста, используйте команды для взаимодействия.");
                }
            }
        } catch (Exception e) {
            log.error("Error handling message from {}: {}", telegramId, e.getMessage(), e);
            messageService.sendMessage(chatId,
                    "Произошла ошибка при обработке твоего сообщения. Попробуй ещё раз.");
        }
    }

    private void handleCallback(Update update) {
        var callback = update.callbackQuery();
        String data = callback.data();
        Long chatId = callback.message().chat().id();
        Long telegramId = callback.from().id();
        String username = callback.from().username() != null ? callback.from().username()
                : callback.from().firstName();
        String firstName = callback.from().firstName();

        User user = userService.getOrCreateUser(telegramId, username, firstName);

        try {
            if (data.startsWith("complete:")) {
                Long taskId = Long.parseLong(data.replace("complete:", ""));
                boolean ok = taskService.completeTask(taskId, user);
                if (ok) {
                    messageService.sendMessage(chatId, "Задача #" + taskId + " выполнена");
                } else {
                    messageService.sendMessage(chatId,
                            "Не удалось пометить задачу как выполненную. Возможно, она уже завершена или принадлежит другому пользователю.");
                }
            } else {
                messageService.sendMessage(chatId, "Неизвестное действие.");
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid callback data: {}", data);
            messageService.sendMessage(chatId, "Неверные данные в запросе.");
        } catch (Exception e) {
            log.error("Error handling callback: {}", e.getMessage(), e);
            messageService.sendMessage(chatId, "Произошла ошибка при обработке действия.");
        } finally {
            telegramBot.execute(new AnswerCallbackQuery(callback.id()));
        }
    }
}

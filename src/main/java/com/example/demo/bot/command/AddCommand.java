package com.example.demo.bot.command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Component;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.service.AddStateService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.MessageService;
import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import com.example.demo.util.UpdateUtils;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class AddCommand implements Command {

    private final CategoryService categoryService;
    private final TaskService taskService;
    private final UserService userService;
    private final MessageService messageService;
    private final AddStateService addStateService;

    @Override
    public String command() {
        return "/add";
    }

    @Override
    public String description() {
        return "Добавить новую задачу";
    }

    @Override
    public void handle(Update update) {
        Long chatId = UpdateUtils.getChatId(update);
        String text = update.message().text().trim();
        User user = UpdateUtils.getUser(userService, update);

        if ("/cancel".equalsIgnoreCase(text)) {
            addStateService.removeState(user.getId());
            messageService.sendMessage(chatId, "Операция отменена.");
            return;
        }

        AddState state = addStateService.getState(user.getId());

        if (text.equals("/add")) {
            List<Category> categories = categoryService.getAllCategories();
            if (categories.isEmpty()) {
                messageService.sendMessage(chatId, "Нет доступных категорий.");
                return;
            }
            ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(
                    categories.stream().map(Category::getName).toArray(String[]::new));
            markup.resizeKeyboard(true).selective(true);

            messageService.sendMessage(chatId, "Выбери категорию для новой задачи:", markup);
            addStateService.putState(user.getId(), new AddState(AddStep.WAITING_CATEGORY));
            return;
        }

        if (state == null) {
            messageService.sendMessage(chatId,
                    "Ошибка! Начни заново с /add (или /cancel чтобы отменить).");
            return;
        }

        switch (state.step) {
            case WAITING_CATEGORY:
                Category category = categoryService.findByName(text).orElse(null);
                if (category == null) {
                    messageService.sendMessage(chatId,
                            "Категория не найдена. Попробуй выбрать из списка или /cancel.");
                    return;
                }
                state.categoryName = category.getName();
                state.step = AddStep.WAITING_TITLE;
                messageService.sendMessage(chatId,
                        "Теперь введи название задачи (макс 200 символов):",
                        new ReplyKeyboardRemove(true));
                addStateService.putState(user.getId(), state);
                return;

            case WAITING_TITLE:
                String title = sanitize(text);
                if (title.isBlank() || title.length() > 200) {
                    messageService.sendMessage(chatId,
                            "Неверное название. Оно не должно быть пустым и длина до 200 символов.");
                    return;
                }
                state.title = title;
                state.step = AddStep.WAITING_DEADLINE;
                messageService.sendMessage(chatId,
                        "Теперь введи дедлайн в формате yyyy-MM-dd HH:mm (например, 2025-09-20 18:00) или /cancel:");
                addStateService.putState(user.getId(), state);
                return;

            case WAITING_DEADLINE:
                try {
                    LocalDateTime deadline = LocalDateTime.parse(text,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    Category cat = categoryService.findByName(state.categoryName).orElseThrow();
                    taskService.createTask(user, cat, state.title, deadline);
                    messageService.sendMessage(chatId, "Задача успешно добавлена");
                    addStateService.removeState(user.getId());
                } catch (DateTimeParseException e) {
                    messageService.sendMessage(chatId,
                            "Неверный формат дедлайна. Попробуй снова (yyyy-MM-dd HH:mm) или /cancel:");
                } catch (IllegalArgumentException e) {
                    messageService.sendMessage(chatId, "Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    log.error("Error creating task: {}", e.getMessage(), e);
                    messageService.sendMessage(chatId,
                            "Не удалось создать задачу. Попробуй позже.");
                    addStateService.removeState(user.getId());
                }
                return;
        }

        messageService.sendMessage(chatId, "Ошибка! Начни заново с /add или используй /cancel");
    }

    private String sanitize(String input) {
        String s = input.trim().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        return s;
    }

    public static class AddState {
        AddStep step;
        String categoryName;
        String title;

        public AddState(AddStep step) {
            this.step = step;
        }
    }

    public enum AddStep {
        WAITING_CATEGORY, WAITING_TITLE, WAITING_DEADLINE
    }
}

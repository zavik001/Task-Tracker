package com.example.demo.util;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.pengrad.telegrambot.model.Update;

public class UpdateUtils {

    public static User getUser(UserService userService, Update update) {
        Long telegramId = null;
        String username = null;
        String firstName = null;

        if (update.message() != null && update.message().from() != null) {
            telegramId = update.message().from().id();
            username =
                    update.message().from().username() != null ? update.message().from().username()
                            : update.message().from().firstName();
            firstName = update.message().from().firstName();
        } else if (update.callbackQuery() != null && update.callbackQuery().from() != null) {
            telegramId = update.callbackQuery().from().id();
            username = update.callbackQuery().from().username() != null
                    ? update.callbackQuery().from().username()
                    : update.callbackQuery().from().firstName();
            firstName = update.callbackQuery().from().firstName();
        }

        if (telegramId == null) {
            throw new IllegalArgumentException("Cannot extract user from update");
        }

        return userService.getOrCreateUser(telegramId, username, firstName);
    }

    public static Long getChatId(Update update) {
        if (update.message() != null && update.message().chat() != null) {
            return update.message().chat().id();
        } else if (update.callbackQuery() != null && update.callbackQuery().message() != null) {
            return update.callbackQuery().message().chat().id();
        }
        throw new IllegalArgumentException("Cannot extract chatId from update");
    }
}

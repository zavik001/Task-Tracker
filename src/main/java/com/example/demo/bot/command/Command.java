package com.example.demo.bot.command;

import com.pengrad.telegrambot.model.Update;

public interface Command {
    String command();

    String description();

    void handle(Update update);
}

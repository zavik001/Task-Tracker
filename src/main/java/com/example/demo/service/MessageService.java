package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class MessageService {

    private final TelegramBot telegramBot;

    public void sendMessage(Long chatId, String text) {
        try {
            SendResponse resp = telegramBot.execute(new SendMessage(chatId, text));
            if (!resp.isOk()) {
                log.warn("Telegram sendMessage failed: {} for chatId {}", resp.errorCode(), chatId);
            }
        } catch (Exception e) {
            log.error("Failed to send message to {}: {}", chatId, e);
        }
    }

    public void sendMessage(Long chatId, String text, Keyboard keyboard) {
        try {
            SendResponse resp =
                    telegramBot.execute(new SendMessage(chatId, text).replyMarkup(keyboard));
            if (!resp.isOk()) {
                log.warn("Telegram sendMessage with keyboard failed: {} for chatId {}",
                        resp.errorCode(), chatId);
            }
        } catch (Exception e) {
            log.error("Failed to send message with keyboard to {}: {}", chatId, e);
        }
    }
}

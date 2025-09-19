package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class BotConfig {

    @Bean
    public TelegramBot telegramBot(@Value("${bot.token}") String token) {
        log.info("Initializing TelegramBot bean");
        return new TelegramBot(token);
    }
}

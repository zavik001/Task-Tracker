package com.example.demo.config;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TelegramExceptionHandler {
    public void handle(Exception e, String context) {
        log.error("Exception in Telegram flow: {} - {}", context, e);
    }
}

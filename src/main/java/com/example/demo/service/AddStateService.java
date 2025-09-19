package com.example.demo.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.demo.bot.command.AddCommand.AddState;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AddStateService {

    private static final long TTL_MILLIS = 30 * 60 * 1000;

    private static class StateHolder {
        final AddState state;
        final long createdAt;

        StateHolder(AddState state) {
            this.state = state;
            this.createdAt = Instant.now().toEpochMilli();
        }
    }

    private final Map<Long, StateHolder> userStates = new ConcurrentHashMap<>();

    public synchronized AddState getState(Long userId) {
        StateHolder holder = userStates.get(userId);
        if (holder == null)
            return null;
        if (isExpired(holder)) {
            userStates.remove(userId);
            return null;
        }
        return holder.state;
    }

    public synchronized void putState(Long userId, AddState state) {
        userStates.put(userId, new StateHolder(state));
    }

    public synchronized void removeState(Long userId) {
        userStates.remove(userId);
    }

    private boolean isExpired(StateHolder h) {
        return Instant.now().toEpochMilli() - h.createdAt > TTL_MILLIS;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanup() {
        long now = Instant.now().toEpochMilli();
        userStates.entrySet().removeIf(e -> now - e.getValue().createdAt > TTL_MILLIS);
        log.debug("AddStateService cleanup executed, remaining states: {}", userStates.size());
    }
}

package com.example.demo.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User getOrCreateUser(Long telegramId, String username, String firstName) {
        Optional<User> userOpt = userRepository.findById(telegramId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean changed = false;
            if (username != null && !username.equals(user.getUsername())) {
                user.setUsername(username);
                changed = true;
            }
            if (firstName != null && !firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                changed = true;
            }
            if (changed) {
                user = userRepository.save(user);
                log.info("Updated user {} (id={})", username, telegramId);
            }
            return user;
        } else {
            User newUser =
                    User.builder().id(telegramId).username(username).firstName(firstName).build();
            userRepository.save(newUser);
            log.info("Created user id={}", telegramId);
            return newUser;
        }
    }
}

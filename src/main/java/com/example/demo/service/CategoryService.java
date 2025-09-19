package com.example.demo.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initDefaultCategories() {
        List<String> defaultNames =
                Arrays.asList("Аналитика", "Разработка", "Дизайн", "Тестирование", "Другое");
        defaultNames.forEach(name -> {
            if (findByName(name).isEmpty()) {
                save(Category.builder().name(name).build());
                log.info("Saved default category: {}", name);
            }
        });
    }
}

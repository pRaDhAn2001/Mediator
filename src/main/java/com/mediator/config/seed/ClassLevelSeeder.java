package com.mediator.config.seed;

import com.mediator.master.entity.ClassLevel;
import com.mediator.master.repository.ClassLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class ClassLevelSeeder implements CommandLineRunner {

    private final ClassLevelRepository classLevelRepository;

    @Override
    public void run(String... args) {

        for (int standard = 1; standard <= 12; standard++) {

            if (!classLevelRepository.existsByStandard(standard)) {

                classLevelRepository.save(
                        ClassLevel.builder()
                                .standard(standard)
                                .build());
            }
        }
    }
}
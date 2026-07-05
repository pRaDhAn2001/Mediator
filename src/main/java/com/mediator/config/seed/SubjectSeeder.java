package com.mediator.config.seed;

import com.mediator.master.entity.Subject;
import com.mediator.master.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(3)
public class SubjectSeeder implements CommandLineRunner {

    private final SubjectRepository subjectRepository;

    @Override
    public void run(String... args) {

        createSubject("Mathematics");
        createSubject("Physics");
        createSubject("Chemistry");
        createSubject("Biology");
        createSubject("Science");
        createSubject("English");
        createSubject("Hindi");
        createSubject("Bengali");
        createSubject("Sanskrit");
        createSubject("History");
        createSubject("Geography");
        createSubject("Political Science");
        createSubject("Economics");
        createSubject("Accountancy");
        createSubject("Business Studies");
        createSubject("Computer Science");
        createSubject("Statistics");
        createSubject("Environmental Studies");
    }

    private void createSubject(String subjectName) {

        if (!subjectRepository.existsByName(subjectName)) {

            subjectRepository.save(
                    Subject.builder()
                            .name(subjectName)
                            .build());
        }
    }
}

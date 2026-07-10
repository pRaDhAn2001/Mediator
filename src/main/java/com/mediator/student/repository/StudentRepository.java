package com.mediator.student.repository;

import com.mediator.auth.entity.User;
import com.mediator.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByUserEmail(String email);

    Optional<Student> findByUser(User user);
}

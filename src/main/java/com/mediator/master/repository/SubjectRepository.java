package com.mediator.master.repository;

import com.mediator.master.entity.Subject;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByName(String name);

    Optional<Subject> findByName(String name);

    List<Subject> findAllById(Iterable<Long> ids);

}

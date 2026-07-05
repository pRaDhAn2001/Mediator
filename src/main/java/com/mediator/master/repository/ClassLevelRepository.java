package com.mediator.master.repository;

import com.mediator.master.entity.ClassLevel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassLevelRepository extends JpaRepository<ClassLevel, Long> {

    boolean existsByStandard(Integer standard);

    Optional<ClassLevel> findByStandard(Integer standard);
}

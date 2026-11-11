package com.mediator.tutor.repository;

import com.mediator.auth.entity.User;
import com.mediator.tutor.entity.Tutor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long>, JpaSpecificationExecutor<Tutor> {

    Optional<Tutor> findByUser(User user);

    Optional<Tutor> findByUser_Email(String email);

    Optional<Tutor> findByUser_Id(Long userId);

    @Query("""
            SELECT DISTINCT t
            FROM Tutor t
            LEFT JOIN FETCH t.user u
            LEFT JOIN FETCH t.academicProfile ap
            WHERE t.profileStatus = com.mediator.tutor.entity.ProfileStatus.LIVE
              AND t.verificationStatus = com.mediator.tutor.entity.VerificationStatus.APPROVED
            """)
    List<Tutor> findSearchableTutors();
}

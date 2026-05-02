package com.travelagency.repository;

import com.travelagency.domain.EmailOtpChallenge;
import com.travelagency.domain.EmailOtpPurpose;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailOtpChallengeRepository extends JpaRepository<EmailOtpChallenge, UUID> {

    Optional<EmailOtpChallenge> findTopByEmailAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            String email, EmailOtpPurpose purpose);

    @Modifying
    @Query("DELETE FROM EmailOtpChallenge c WHERE c.email = :email AND c.purpose = :purpose")
    void deleteByEmailAndPurpose(@Param("email") String email, @Param("purpose") EmailOtpPurpose purpose);
}

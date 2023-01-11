package com.adissu.reserve.repository;

import com.adissu.reserve.entity.MailActivation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailActivationRepository extends JpaRepository<MailActivation, Integer> {
    Optional<MailActivation> getByActivationCodeAndEmail(String code, String email);
    void deleteByEmail(String email);
    boolean existsByEmail(String email);

    MailActivation findByEmail(String email);
}

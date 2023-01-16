package com.adissu.reserve.repository;

import com.adissu.reserve.entity.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, Integer> {

    Optional<InviteCode> findByClient_Id(int clientId);
    Optional<InviteCode> findByInvCode(String invCode);
    List<InviteCode> findAllByClient_Email(String email);
}

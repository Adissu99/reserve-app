package com.adissu.reserve.repository;

import com.adissu.reserve.entity.AdminConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminConfigRepository extends JpaRepository<AdminConfig, Integer> {

    Optional<AdminConfig> findByName(String configName);
}

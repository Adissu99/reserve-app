package com.adissu.reserve.repository;

import com.adissu.reserve.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    public Optional<Client> getByEmail(String email);
    public Optional<List<Client>> findAllByCodeUsedToRegisterIsIn(List<String> codes);
}
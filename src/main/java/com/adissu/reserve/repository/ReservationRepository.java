package com.adissu.reserve.repository;

import com.adissu.reserve.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Query("SELECT r FROM Reservation r WHERE date(r.selectedDate) = :selectedDate")
    Optional<List<Reservation>> findAllBySelectedDate(@Param("selectedDate") Date selectedDate);

    Optional<List<Reservation>> findAllByClient_Email(String email);
}

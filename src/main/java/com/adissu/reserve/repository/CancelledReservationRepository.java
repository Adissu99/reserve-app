package com.adissu.reserve.repository;

import com.adissu.reserve.entity.CancelledReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancelledReservationRepository extends JpaRepository<CancelledReservation, Integer> {

    List<CancelledReservation> findAllByClient_Id(int clientId);
    List<CancelledReservation> findAllByRequestedAndDone(boolean requested, boolean done);

}

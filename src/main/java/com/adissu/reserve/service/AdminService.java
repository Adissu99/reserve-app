package com.adissu.reserve.service;

import com.adissu.reserve.entity.CancelledReservation;
import com.adissu.reserve.entity.Reservation;
import com.adissu.reserve.repository.CancelledReservationRepository;
import com.adissu.reserve.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final CancelledReservationRepository cancelledReservationRepository;
    private final ReservationRepository reservationRepository;

    public String approveCancelRequest(final String cancelId) {

        Optional<CancelledReservation> cancelledReservationOptional = cancelledReservationRepository.findById(Integer.parseInt(cancelId));
        if( cancelledReservationOptional.isEmpty() ) {
            log.info("Cancel request not found for id {}", cancelId);
            return "ERROR.NOT_FOUND";
        }

        Optional<Reservation> reservation = reservationRepository.findBySelectedDateAndSelectedTime(cancelledReservationOptional.get().getReservationDate(), cancelledReservationOptional.get().getReservationHour());
        if( reservation.isEmpty() ) {
            log.info("Could not find reservation made on date {} and hour {}", cancelledReservationOptional.get().getReservationDate(), cancelledReservationOptional.get().getReservationHour());
            return "ERROR.NOT_FOUND";
        }

        CancelledReservation cancelledReservation = cancelledReservationOptional.get();
        cancelledReservation.setDone(true);
        cancelledReservationRepository.save(cancelledReservation);

        reservationRepository.delete(reservation.get());

        log.info("Successfully cancelled reservation.");
        return "SUCCESS";
    }

}

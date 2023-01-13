package com.adissu.reserve.service;

import com.adissu.reserve.entity.CancelledReservation;
import com.adissu.reserve.entity.Client;
import com.adissu.reserve.entity.InviteCode;
import com.adissu.reserve.entity.Reservation;
import com.adissu.reserve.repository.CancelledReservationRepository;
import com.adissu.reserve.repository.ClientRepository;
import com.adissu.reserve.repository.InviteCodeRepository;
import com.adissu.reserve.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final CancelledReservationRepository cancelledReservationRepository;
    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final KeyCloakService keyCloakService;

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

    public String makeAdmin(String clientId) {
        Optional<Client> client = clientRepository.findById(Integer.parseInt(clientId));
        if( client.isEmpty() ) {
            return "ERROR.NOT_FOUND";
        }

        keyCloakService.makeAdmin(client.get().getEmail());
        client.get().setRole("admin");
        clientRepository.save(client.get());
        return "SUCCESS";
    }

    public List<Client> getClientInvitedChain(String clientId) {
        Optional<Client> clientOptional = clientRepository.findById(Integer.parseInt(clientId));
        if( clientOptional.isEmpty() ) {
            log.info("Client with id {} could not be found.", clientId);
            return new ArrayList<>();
        }

        String invCode = clientOptional.get().getCodeUsedToRegister();
        Optional<InviteCode> inviteCodeOptional = inviteCodeRepository.findByInvCode(invCode);
        List<Client> clientList = new ArrayList<>();

        while( inviteCodeOptional.isPresent() ) {
            clientList.add(inviteCodeOptional.get().getClient());
            invCode = inviteCodeOptional.get().getClient().getCodeUsedToRegister();
            inviteCodeOptional = inviteCodeRepository.findByInvCode(invCode);
        }

        return clientList;
    }

    public List<Client> getInvitedUsersList() {
        return clientRepository.findAllByCodeUsedToRegisterIsNot("ADMIN");
    }

    public HashMap<Integer, Integer> getCancelledReservationsUsers(List<Client> clientList) {
        HashMap<Integer, Integer> cancelledReservationsMap = new HashMap<>();

        for( Client client : clientList ) {
            List<CancelledReservation> cancelledReservations = cancelledReservationRepository.findAllByClient_Id(client.getId());
            cancelledReservationsMap.put(client.getId(), cancelledReservations.size());
        }

        return cancelledReservationsMap;
    }

}

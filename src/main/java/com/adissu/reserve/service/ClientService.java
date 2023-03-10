package com.adissu.reserve.service;

import com.adissu.reserve.constants.ResultConstants;
import com.adissu.reserve.dto.ClientDTO;
import com.adissu.reserve.dto.MailDTO;
import com.adissu.reserve.entity.*;
import com.adissu.reserve.repository.*;
import com.adissu.reserve.util.ClientUtil;
import com.adissu.reserve.util.InviteCodeUtil;
import com.adissu.reserve.util.MailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final MailActivationRepository mailActivationRepository;
    private final ReservationRepository reservationRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final CancelledReservationRepository cancelledReservationRepository;
    private final ClientUtil clientUtil;
    private final MailUtil mailUtil;
    private final InviteCodeUtil inviteCodeUtil;
    private final KeyCloakService keyCloakService;

    public Client registerClient(final ClientDTO clientDTO) {
        log.info("Trying to register client..");
        if( !clientUtil.validateRegistration(clientDTO) ) {
            return null;
        }
        log.info("Validated inputs.");
        if( !inviteCodeUtil.isInviteCodeValid(clientDTO.getCodeUsedToRegister()) ) {
            return null;
        }
        log.info("Validated code.");
        if( clientRepository.getByEmail(clientDTO.getEmail()).isPresent() ) {
            log.info("Email already exists.");
            return null;
        }
        log.info("Validated email.");
        Client client = clientUtil.mapDtoToEntity(clientDTO);
        log.info("Mapped client");
        inviteCodeUtil.useInviteCode(client.getCodeUsedToRegister());

        log.info("Adding user to KeyCloak..");
        keyCloakService.addUser(clientDTO);
        log.info("Added user to KeyCloak.");

        MailActivation mailActivation = MailActivation.builder()
                .email(clientDTO.getEmail())
                .issuedAt(new Date())
                .activationCode(mailUtil.generateActivationCode())
                .activated(false)
                .build();
        mailActivationRepository.save(mailActivation);

        if (mailUtil.sendActivationMail(mailActivation).equals(ResultConstants.SUCCESS)) {
            log.info("Email sent successfully!");
        } else {
            log.info("There was a problem sending the email.");
        }

        client.setRole("client");
        return clientRepository.save(client);
    }

    public List<String> findAllClients() {
        List<String> clients = new ArrayList<>();
        for( Client client : clientRepository.findAll() ) {
            clients.add(clientUtil.getRelevantClientInfo(client));
        }
        return clients;
    }

    public String getClientInfo(int clientId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        if( clientOptional.isEmpty() ) {
            return null;
        }

        return clientUtil.getRelevantClientInfo(clientOptional.get());
    }

    // Test method
    public boolean sendMail(final MailDTO mailDTO) {
        return mailUtil.sendMail(mailDTO).equals(ResultConstants.SUCCESS);
    }

    // activate a user based on the email and the code
    public String activateUser(String email, String code) {

        log.info("Got email: {} ; And code: {}", email, code);

        Optional<MailActivation> mailActivationOptional = mailActivationRepository.getByActivationCodeAndEmail(code, email);
        if( mailActivationOptional.isEmpty() ) {
            return ResultConstants.ERROR_NOT_FOUND;
        }

        if(mailActivationOptional.get().getActivated()) {
            return ResultConstants.ERROR_ALREADY_ACTIVATED;
        }

        log.info("Got mailActivation : {}", mailActivationOptional.get());
        keyCloakService.verifyEmail(email);

        mailActivationOptional.get().setActivated(true);
        mailActivationRepository.save(mailActivationOptional.get());

        return ResultConstants.SUCCESS;
    }

    // resend activation mail. Username is the same as email
    public String resendMail(String email) {
        if( mailActivationRepository.existsByEmail(email) ) {
            mailActivationRepository.deleteByEmail(email);

            MailActivation mailActivation = MailActivation.builder()
                    .email(email)
                    .issuedAt(new Date())
                    .activationCode(mailUtil.generateActivationCode())
                    .activated(false)
                    .build();
            mailActivationRepository.save(mailActivation);

            if (mailUtil.sendActivationMail(mailActivation).equals(ResultConstants.SUCCESS)) {
                log.info("Email resent successfully!");
                return ResultConstants.SUCCESS_RESENT;
            } else {
                log.info("There was a problem resending the email.");
                return ResultConstants.ERROR_RESENDING;
            }
        } else {
            log.info("Email {} was not found.", email);
            return ResultConstants.ERROR_NOT_FOUND;
        }
    }

    public List<Client> getInvitedUsersList(String clientEmail) {
        List<InviteCode> optionalInviteCodeList = inviteCodeRepository.findAllByClient_Email(clientEmail);
        if( optionalInviteCodeList.isEmpty() ) {
            return null;
        }

        List<Client> optionalInvitedClientList = clientRepository.findAllByCodeUsedToRegisterIsIn(optionalInviteCodeList
                                                                                                                .stream()
                                                                                                                .map(InviteCode::getInvCode)
                                                                                                                .collect(Collectors.toList()));

        if( optionalInvitedClientList.isEmpty() ) {
            return null;
        }

        return optionalInvitedClientList;
    }

    // HashMap<Email, isActivated?>
    public HashMap<String, Boolean> getClientsActivatedFromList(List<Client> clientsToCheck) {
        if( clientsToCheck == null ) {
            return new HashMap<>();
        }

        HashMap<String, Boolean> clients = new HashMap<>();
        boolean isActivated;
        for( String email : clientsToCheck.stream().map(Client::getEmail).toList() ) {
            isActivated = mailActivationRepository.findByEmail(email).getActivated();
            clients.put(email, isActivated);
        }

        return clients;
    }

    public List<Reservation> getReservationsMade(String clientEmail) {
        List<Reservation> reservationList = reservationRepository.findAllByClient_Email(clientEmail);

        if( reservationList.isEmpty() ) {
            return null;
        }

        return reservationList;
    }

    public Client getClient(String clientEmail) {
        Optional<Client> client = clientRepository.getByEmail(clientEmail);

        if( client.isEmpty() ) {
            return null;
        }

        return client.get();
    }

    public List<CancelledReservation> getCancelledReservations(String email) {
        Client client = getClient(email);
        if( client == null ) {
            return null;
        }

        List<CancelledReservation> cancelledReservationOptional = cancelledReservationRepository.findAllByClient_Id(client.getId());
        if( cancelledReservationOptional.isEmpty() ) {
            return null;
        }

        return cancelledReservationOptional;
    }

}

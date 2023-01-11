package com.adissu.reserve.service;

import com.adissu.reserve.dto.ClientDTO;
import com.adissu.reserve.dto.MailDTO;
import com.adissu.reserve.entity.Client;
import com.adissu.reserve.entity.InviteCode;
import com.adissu.reserve.entity.MailActivation;
import com.adissu.reserve.entity.Reservation;
import com.adissu.reserve.repository.ClientRepository;
import com.adissu.reserve.repository.InviteCodeRepository;
import com.adissu.reserve.repository.MailActivationRepository;
import com.adissu.reserve.repository.ReservationRepository;
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

        if (mailUtil.sendActivationMail(mailActivation)) {
            log.info("Email sent successfully!");
        } else {
            log.info("There was a problem sending the email.");
        }

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
        return mailUtil.sendMail(mailDTO);
    }

    // activate a user based on the email and the code
    public String activateUser(String email, String code) {

        log.info("Got email: {} ; And code: {}", email, code);

        Optional<MailActivation> mailActivationOptional = mailActivationRepository.getByActivationCodeAndEmail(code, email);
        if( mailActivationOptional.isEmpty() ) {
            return "ERROR";
        }

        if(mailActivationOptional.get().getActivated()) {
            return "ACTIVATED";
        }

        log.info("Got mailActivation : {}", mailActivationOptional.get());
        keyCloakService.verifyEmail(email);

        mailActivationOptional.get().setActivated(true);
        mailActivationRepository.save(mailActivationOptional.get());

        return "SUCCESS";
    }

    // resend activation mail. Username is the same as email
    public String resendMail(String email) {
        String result = "";
        if( mailActivationRepository.existsByEmail(email) ) {
            mailActivationRepository.deleteByEmail(email);

            MailActivation mailActivation = MailActivation.builder()
                    .email(email)
                    .issuedAt(new Date())
                    .activationCode(mailUtil.generateActivationCode())
                    .activated(false)
                    .build();
            mailActivationRepository.save(mailActivation);

            if (mailUtil.sendActivationMail(mailActivation)) {
                log.info("Email resent successfully!");
                result = "RESENT";
            } else {
                log.info("There was a problem resending the email.");
                result = "ERROR.RESENDING";
            }
        } else {
            log.info("Email {} was not found.", email);
            result = "NOT-FOUND";
        }

        return result;
    }

    public List<Client> getInvitedUsersList(String clientEmail) {
        Optional<List<InviteCode>> optionalInviteCodeList = inviteCodeRepository.findAllByClient_Email(clientEmail);
        if( optionalInviteCodeList.isEmpty() ) {
            return null;
        }

        Optional<List<Client>> optionalInvitedClientList = clientRepository.findAllByCodeUsedToRegisterIsIn(optionalInviteCodeList.get()
                                                                                                                .stream()
                                                                                                                .map(InviteCode::getInvCode)
                                                                                                                .collect(Collectors.toList()));

        if( optionalInvitedClientList.isEmpty() ) {
            return null;
        }

        return optionalInvitedClientList.get();
    }

    // HashMap<Email, isActivated?>
    public HashMap<String, Boolean> getClientsActivatedFromList(List<Client> clientsToCheck) {
        HashMap<String, Boolean> clients = new HashMap<>();
        boolean isActivated = false;
        for( String email : clientsToCheck.stream().map(Client::getEmail).toList() ) {
            isActivated = mailActivationRepository.findByEmail(email).getActivated();
            clients.put(email, isActivated);
        }

        return clients;
    }

    public List<Reservation> getReservationsMade(String clientEmail) {
        Optional<List<Reservation>> reservationList = reservationRepository.findAllByClient_Email(clientEmail);

        if( reservationList.isEmpty() ) {
            return null;
        }

        return reservationList.get();
    }

    public Client getClient(String clientEmail) {
        Optional<Client> client = clientRepository.getByEmail(clientEmail);

        if( client.isEmpty() ) {
            return null;
        }

        return client.get();
    }
}

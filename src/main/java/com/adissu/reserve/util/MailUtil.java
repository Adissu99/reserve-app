package com.adissu.reserve.util;

import com.adissu.reserve.dto.MailDTO;
import com.adissu.reserve.entity.Client;
import com.adissu.reserve.entity.MailActivation;
import com.adissu.reserve.entity.Reservation;
import com.adissu.reserve.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailUtil {

    private final JavaMailSender emailSender;
    private static final RandomString randomString = new RandomString(30);
    private final ClientRepository clientRepository;

    public boolean sendMail(MailDTO mailDTO) {
        if (!validateMailDTO(mailDTO)) {
            return false;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailDTO.getTo());
        message.setSubject(mailDTO.getSubject());
        message.setText(mailDTO.getText());

        emailSender.send(message);
        return true;
    }

    private String getActivationLink(MailActivation mailActivation) {
        StringBuilder link = new StringBuilder("http://");

        link.append("localhost:8080")
                .append("/api")
                .append("/client")
                .append("/activate")
                .append("?email=").append(mailActivation.getEmail())
                .append("&code=").append(mailActivation.getActivationCode());

        log.info("Link to be sent: {}", link.toString());

        return link.toString();
    }

    public String generateActivationCode() {
        return randomString.nextString();
    }

    private boolean validateMailDTO(MailDTO mailDTO) {
        return true;
    }

    public boolean sendActivationMail(MailActivation mailActivation) {
        final String link = getActivationLink(mailActivation);
        MailDTO mailDTO = MailDTO.builder()
//                .to(mailActivation.getEmail())
                .to(mailActivation.getEmail())
                .subject("Please verify your email address")
                .text("Hi! Please click the following link to verify your email address:\n\n" + link)
                .build();

        return sendMail(mailDTO);
    }

    public boolean sendRequestCancelReservation(Reservation reservation) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Client client = reservation.getClient();
        String text = new StringBuilder()
                .append(String.format("Client %s %s with email %s", client.getFirstName(), client.getLastName(), client.getEmail()))
                .append(String.format(" has requested a cancellation for reservation made on %s at %s for product %s.", simpleDateFormat.format(reservation.getSelectedDate()), reservation.getSelectedTime(), reservation.getProduct().getProductName()))
                .append(" Please review it as soon as possible!")
                .toString();

        List<String> adminsEmailList = getAdminsEmailList();

        if( adminsEmailList.isEmpty() ) {
            log.info("Admins Email List is empty.");
            return false;
        }

        for( String adminEmail : adminsEmailList ) {
            MailDTO mailDTO = MailDTO.builder()
                    .to(adminEmail)
                    .subject("Cancellation Request")
                    .text(text)
                    .build();

            if(!sendMail(mailDTO)) {
                log.info("There was an error while trying to send emails to admins.");
                return false;
            }
        }

        return true;
    }

    private List<String> getAdminsEmailList() {
        List<String> adminsEmailList = new ArrayList<>();
        List<Client> clientList = clientRepository.findAllByRole("admin");
        if( !clientList.isEmpty() ) {
            for( Client client : clientList ) {
                adminsEmailList.add(client.getEmail());
            }
        }

        return adminsEmailList;
    }

}

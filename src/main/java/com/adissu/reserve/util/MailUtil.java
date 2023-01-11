package com.adissu.reserve.util;

import com.adissu.reserve.dto.MailDTO;
import com.adissu.reserve.entity.MailActivation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailUtil {

    private final JavaMailSender emailSender;
    private static final RandomString randomString = new RandomString(30);

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


}

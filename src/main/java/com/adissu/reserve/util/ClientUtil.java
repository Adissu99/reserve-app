package com.adissu.reserve.util;

import com.adissu.reserve.dto.ClientDTO;
import com.adissu.reserve.entity.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;

@Component
@Slf4j
public class ClientUtil {

    public Boolean validateRegistration(final ClientDTO clientDTO) {
        if( clientDTO.getFirstName() == null || clientDTO.getFirstName().replaceAll(" ", "").equals("") ) {
            log.info("First Name is empty.");
            return false;
        } else if( clientDTO.getLastName() == null || clientDTO.getLastName().replaceAll(" ", "").equals("") ) {
            log.info("Last Name is empty.");
            return false;
        } else if( clientDTO.getEmail() == null || clientDTO.getEmail().replaceAll(" ", "").equals("") ) {
            log.info("Email is empty.");
            return false;
        } else if( !RegistrationUtil.isPasswordComplexityValid(clientDTO.getPassword()) ) {
            log.info("""
                            Password must contain:
                            At least one digit [0-9]
                            At least one lowercase Latin character [a-z]
                            At least one uppercase Latin character [A-Z]
                            At least one special character like ! @ # & ( )
                            A length of at least 8 characters and a maximum of 20 characters"""
                    );
            return false;
        } else if( clientDTO.getCodeUsedToRegister() == null || clientDTO.getCodeUsedToRegister().replaceAll(" ", "").equals("") ) {
            log.info("Registration Code is empty.");
            return false;
        }

        return true;
    }

    public Client mapDtoToEntity(final ClientDTO clientDTO) {
        return Client.builder()
                .firstName(clientDTO.getFirstName())
                .lastName(clientDTO.getLastName())
                .email(clientDTO.getEmail())
                .codeUsedToRegister(clientDTO.getCodeUsedToRegister())
                .invCodes(new ArrayList<>())
                .registeredAt(new Date())
                .password(clientDTO.getPassword())
                .build();
    }

    public String getRelevantClientInfo(final Client client) {

        return new StringBuilder()
                .append("Name: ").append(client.getFullName())
                .append("\nEmail: ").append(client.getEmail())
                .append("\nRegistered at: ").append(client.getRegisteredAt())
                .append("\nCode used to register: ").append(client.getCodeUsedToRegister())
                .toString();
    }

}

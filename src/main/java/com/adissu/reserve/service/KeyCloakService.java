package com.adissu.reserve.service;

import com.adissu.reserve.dto.ClientDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyCloakService {

    private final RealmResource realmResource;

    public void addUser(ClientDTO clientDTO) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(clientDTO.getEmail());
        userRepresentation.setFirstName(clientDTO.getFirstName());
        userRepresentation.setLastName(clientDTO.getLastName());
        userRepresentation.setEmail(clientDTO.getEmail());
//        userRepresentation.setEnabled(true);      // after email confirmation
        log.info("clientDTO has been used to set values for user.\nTrying to create him..");
        Response response = realmResource.users().create(userRepresentation);
        log.info("User created, now setting password");
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        log.info("Trying to get userId from response");
        String userId = CreatedResponseUtil.getCreatedId(response);
        log.info("Got userId {} from response", userId);
        passwordCred.setTemporary(false);
        passwordCred.setType("password");
        passwordCred.setValue(clientDTO.getPassword()); // parola
        log.info("Getting userResource from userId {}", userId);
        UserResource userResource = realmResource.users().get(userId);
        userResource.resetPassword(passwordCred);

        log.info("Trying to get role client and assign it to new user");
        RoleRepresentation roleRepresentation = realmResource.roles().get("client").toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));
    }

    public void verifyEmail(String email) {
        log.info("Verifying user with email address {}", email);
        UserRepresentation userRepresentation = realmResource.users().search(email).get(0);
        log.info("Got userRepresentation {}", userRepresentation.toString());
        UserResource userResource = realmResource.users().get(userRepresentation.getId());

        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        log.info("Trying to update user..");
        userResource.update(userRepresentation);
        log.info("User updated");
    }

    public void makeAdmin(String email) {
        UserRepresentation userRepresentation = realmResource.users().search(email).get(0);
        log.info("Got userRepresentation {}", userRepresentation.toString());
        UserResource userResource = realmResource.users().get(userRepresentation.getId());

        RoleRepresentation roleRepresentation = realmResource.roles().get("admin").toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));

        log.info("Trying to update user..");
        userResource.update(userRepresentation);
        log.info("User updated");
    }
}

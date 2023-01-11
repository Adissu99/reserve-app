package com.adissu.reserve.config;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class KeycloakConfig {

    public Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                    .serverUrl("http://localhost:8180")
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .realm("adissu")
                    .clientId("keycloak-admin")
                    .clientSecret("Wzi7b4ZVW0qDEGQ1XfTV1k39ELK699yM")
                    .resteasyClient(
                            new ResteasyClientBuilderImpl()
                                    .connectionPoolSize(10)
                                    .build())
                    .build();
    }

    @Bean
    public RealmResource getRealm() {
        return getKeycloakInstance().realm("adissu");
    }
}

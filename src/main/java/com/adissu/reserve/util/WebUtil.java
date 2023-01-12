package com.adissu.reserve.util;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakSecurityContext;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class WebUtil {

    public static String getUsername(HttpServletRequest httpServletRequest) {
        String username = "";
        KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        if( keycloakSecurityContext == null ) {
            log.info("Security Context is null.");
        } else {
            username = keycloakSecurityContext.getIdToken().getPreferredUsername();
        }

        return username;
    }

    public static String formatDateString(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        try {
            date1  = simpleDateFormat1.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        log.info("Date after format: {}", simpleDateFormat.format(date1));
        return date1 != null ? simpleDateFormat.format(date1) : date;
    }

    public static List<String> getAvailableDurationsForProducts() {
        List<String> availableDurations = new ArrayList<>();
        int currHour = 0;
        String duration = "";
        // maxim 4h / 240min.
        for( int i = 15; i<= 240; i+= 15) {
            if( i%60 == 0 ) {
                currHour++;
            }

            duration = i + "min";

            if( currHour > 0 ) {
                duration += " / " + currHour + "h";
                if( i-currHour*60 > 0 ) {
                    duration += ":" + (i-currHour*60) + "m";
                }
            }

            availableDurations.add(duration);
        }

        return availableDurations;
    }

}

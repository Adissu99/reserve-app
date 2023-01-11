package com.adissu.reserve.controller.api;

import com.adissu.reserve.dto.ReserveDTO;
import com.adissu.reserve.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/api/reserve/")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping(path = "new")
    @RolesAllowed({"client", "admin"})
    public ResponseEntity<String> reserve(@RequestBody ReserveDTO reserveDTO) {

        if( !reservationService.reserveTest(reserveDTO.getProductId(), reserveDTO.getClientId(), reserveDTO.getSelectedTime()) ) {
            return ResponseEntity.badRequest().body("Request is invalid.");
        }

        return ResponseEntity.ok("Successfully Reserved!");
    }
}

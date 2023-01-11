package com.adissu.reserve.controller.api;

import com.adissu.reserve.entity.InviteCode;
import com.adissu.reserve.service.InviteCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(path = "/api/invite/generate-code/")
@RequiredArgsConstructor
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;

    @GetMapping(path = "{clientId}")
    public ResponseEntity<String> generateCodeForClient(@PathVariable int clientId) {
        Optional<InviteCode> inviteCode = inviteCodeService.generateInviteCode(clientId);
        if( inviteCode.isEmpty() ) {
            return ResponseEntity.internalServerError().body(String.format("Oops, something went wrong! Client with id %s not found or maximum number of invite codes per month has been reached.", clientId));
        }

        return ResponseEntity.ok(String.format("Generated invite code %s \nfor client with id %s and named: %s", inviteCode.get().getInvCode(), inviteCode.get().getClient().getId(), inviteCode.get().getClient().getFullName()));
    }
}

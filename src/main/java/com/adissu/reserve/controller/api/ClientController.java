package com.adissu.reserve.controller.api;

import com.adissu.reserve.dto.ClientDTO;
import com.adissu.reserve.dto.MailDTO;
import com.adissu.reserve.entity.Client;
import com.adissu.reserve.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping(path = "/api/client/")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @PostMapping(path = "register", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> registerClient(@RequestBody ClientDTO clientDTO) {
        log.info("Got client: {}", clientDTO);
        Client client = clientService.registerClient(clientDTO);

        if (client == null) {
            return ResponseEntity.badRequest().body("Invalid request.");
        }

        return ResponseEntity.ok(clientDTO.toString());
    }

    @GetMapping(path = "find-all", produces = MediaType.TEXT_PLAIN_VALUE)
    @RolesAllowed("admin")
    public ResponseEntity<String> findAllClients() {
        StringBuilder sb = new StringBuilder();
        for(String client : clientService.findAllClients()) {
            sb.append(client).append("\n\n").toString();
        }
        return ResponseEntity.ok(sb.toString());
    }

    @GetMapping(path = "{clientId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @RolesAllowed("client")
    public ResponseEntity<String> getClientInformation(@PathVariable int clientId) {
        String clientInfo = clientService.getClientInfo(clientId);

        if( clientInfo == null ) {
            return ResponseEntity.badRequest().body("Invalid request.");
        }

        return ResponseEntity.ok(clientInfo);
    }

    @PostMapping(path = "send-mail", produces = MediaType.TEXT_PLAIN_VALUE)
    @RolesAllowed("admin")
    public ResponseEntity<String> sendMail(@RequestBody MailDTO mailDTO) {
        if (!clientService.sendMail(mailDTO)) {
            return ResponseEntity.badRequest().body("Error. Mail wasn't sent.");
        }

        return ResponseEntity.ok("Mail sent successfully.");
    }

    /*@GetMapping(path = "activate") // allow everyone
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String code) {
        String result = clientService.activateUser(email, code);
        if ( result.equals("ERROR") ) {
            return ResponseEntity.badRequest().body("Error. Email was not verified.");
        }

        if( result.equals("ACTIVATED") ) {
            return ResponseEntity.badRequest().body("Error. Email was already verified.");
        }

        return ResponseEntity.ok("Email Verified.");
    }*/
}

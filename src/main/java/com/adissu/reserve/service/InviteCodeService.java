package com.adissu.reserve.service;

import com.adissu.reserve.constants.AdminConfigConstants;
import com.adissu.reserve.entity.AdminConfig;
import com.adissu.reserve.entity.Client;
import com.adissu.reserve.entity.InviteCode;
import com.adissu.reserve.repository.ClientRepository;
import com.adissu.reserve.repository.InviteCodeRepository;
import com.adissu.reserve.util.InviteCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final ClientRepository clientRepository;
    private final InviteCodeUtil inviteCodeUtil;
    private final List<AdminConfig> adminConfigList;

    public Optional<InviteCode> generateInviteCode(int clientId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);

        return doGenerate(clientOptional);
    }

    public Optional<InviteCode> generateInviteCode(String email) {
        Optional<Client> clientOptional = clientRepository.getByEmail(email);

        return doGenerate(clientOptional);
    }

    private Optional<InviteCode> doGenerate(Optional<Client> clientOptional) {
        if( clientOptional.isEmpty() ) {
            return Optional.empty();
        }
        Client client = clientOptional.get();

        int maxInviteCodes = Integer.parseInt(adminConfigList.stream()
                .filter(adminConfig -> adminConfig.getName().equals(AdminConfigConstants.MAX_INVITES_MONTHLY))
                .findFirst()
                .get().getValue());

        if( getInviteCodes(client.getEmail()).size() >= maxInviteCodes ) {
            return Optional.empty();
        }

        String invCode = inviteCodeUtil.generateInviteCode(client);
        log.info("Encrypted Code: {}", invCode);

        InviteCode inviteCode = InviteCode.builder()
                .client(client)
                .invCode(invCode)
                .isUsed(false)
                .generatedAt(new Date())
                .build();

        client.getInvCodes().add(inviteCode);

        return Optional.of(inviteCodeRepository.save(inviteCode));
    }

    public List<InviteCode> getInviteCodes(String email) {
        Optional<List<InviteCode>> optionalInviteCodes = inviteCodeRepository.findAllByClient_Email(email);

        if( optionalInviteCodes.isEmpty() ) {
            return null;
        }

        return optionalInviteCodes.get();
    }

}

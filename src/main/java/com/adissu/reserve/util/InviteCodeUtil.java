package com.adissu.reserve.util;

import com.adissu.reserve.entity.Client;
import com.adissu.reserve.entity.InviteCode;
import com.adissu.reserve.repository.InviteCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class InviteCodeUtil {

    private final InviteCodeRepository inviteCodeRepository;
    private final AESUtil aesUtil;

    public Boolean isInviteCodeValid(String invCode) {
        if( invCode == null ) {
            return null;
        }

        if( !invCode.equals("ADMIN") ) {
            Optional<InviteCode> inviteCodeOptional = inviteCodeRepository.findByInvCode(invCode);
            if( inviteCodeOptional.isEmpty() ) {
                log.info("Invite code {} doesn't exist.", invCode);
                return false;
            }

            if( inviteCodeOptional.get().getIsUsed() ) {
                log.info("Invite code {} has been already used.", invCode);
                return false;
            }
        }

        return true;
    }

    public String generateInviteCode(Client client) {
        String originalCode = new StringBuilder()
                .append(client.getFirstName())
                .append(client.getLastName())
                .append(client.getInvCodes().size()+1).toString();

        log.info("Original code: {}", originalCode);

        return aesUtil.encrypt(originalCode, client.getEmail());
    }

    public void useInviteCode(String invCode) {
        if( !invCode.equals("ADMIN") ) {
            Optional<InviteCode> inviteCodeOptional = inviteCodeRepository.findByInvCode(invCode);
            InviteCode inviteCode = inviteCodeOptional.get();
            inviteCode.setIsUsed(true);
            inviteCodeRepository.save(inviteCode);
            log.info("Set invite code {} as used.", invCode);
        }
    }
}

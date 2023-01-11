package com.adissu.reserve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientDTO {

    private String firstName;
    private String lastName;
    private String codeUsedToRegister;
    private String email;
    private String password;
}

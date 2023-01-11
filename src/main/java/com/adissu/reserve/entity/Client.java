package com.adissu.reserve.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Client {

    @Id
    @SequenceGenerator(
            name = "client_id_sequence",
            sequenceName = "client_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_id_sequence"
    )
    private int id;
    private String firstName;
    private String lastName;
    private String email;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<InviteCode> invCodes;
    private String codeUsedToRegister;
    private Date registeredAt;
    private String password;

    public String getFullName() {
        return firstName + " " + lastName;
    }

}

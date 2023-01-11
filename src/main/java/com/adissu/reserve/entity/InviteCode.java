package com.adissu.reserve.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteCode {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;
    private String invCode;
    private Boolean isUsed;
    private Date generatedAt;
}

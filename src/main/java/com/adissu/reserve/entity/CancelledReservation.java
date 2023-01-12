package com.adissu.reserve.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CancelledReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    private Date reservationDate;
    private String reservationHour;
    private String reservationProduct;

    private Date cancelDate;
    private boolean requested;
    private boolean done;

}

package com.adissu.reserve.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    // name of the configuration. Ex: FIRST_HOUR; LAST_HOUR; MAX_INVITES_MONTHLY
    private String name;

    // the actual value of the configuration. Ex: 08:00; 18:00; 3
    private String value;
}

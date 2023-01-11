package com.adissu.reserve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveDTO {

    private int productId;
    private int clientId;
    private String selectedTime;
}

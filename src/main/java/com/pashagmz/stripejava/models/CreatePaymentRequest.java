package com.pashagmz.stripejava.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private String paymentMethodId;

    private Boolean isConfirmed;
}

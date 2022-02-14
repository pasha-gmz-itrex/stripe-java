package com.pashagmz.stripejava.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfirmPaymentRequest {

    private String paymentIntentId;
}

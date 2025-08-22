package com.example.finance.dto;

import com.example.finance.entity.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private String senderEmail;
    private String receiverEmail;
    private BigDecimal amount;
    private Timestamp timestamp;
    private String description;
    private Type type;
    private BigDecimal oldBalanceOrg;
    private BigDecimal newBalanceOrig;
    private BigDecimal oldBalanceDest;
    private BigDecimal newBalanceDest;

    private boolean isFraudSuspected;
    private double fraudProbability;
    private boolean requiredManualReview;
    private String fraudDetectionError;
}

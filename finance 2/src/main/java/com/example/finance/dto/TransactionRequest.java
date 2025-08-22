package com.example.finance.dto;


import com.example.finance.entity.Type;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class TransactionRequest {
    private String senderEmail;
    private String receiverEmail;
    private BigDecimal amount;
    private Timestamp timestamp;
    private String description;
    private Type type;
}

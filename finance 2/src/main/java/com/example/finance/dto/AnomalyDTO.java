package com.example.finance.dto;

import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long transactionId;
    private String anomalyType;
    private Double score;
    private String description;
    private Timestamp detectedAt;
    private Boolean resolved;
}


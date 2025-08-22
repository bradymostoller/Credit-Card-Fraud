package com.example.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;


    private BigDecimal amount;
    private Timestamp timestamp;
    private String description;

    private BigDecimal oldBalanceOrg;
    private BigDecimal newBalanceOrig;
    private BigDecimal oldBalanceDest;
    private BigDecimal newBalanceDest;

    @Enumerated(EnumType.STRING)
    private Type type;
}

package com.example.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "anomalies")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="transaction_id")
    Transaction transaction;

    @ManyToOne
    @JoinColumn(name="user_id")
    User user;

    private String anomalyType;
    private Double score;
    private String description;
    private Timestamp detectedAt;
    private Boolean resolved = false;
    private Timestamp resolvedAt;
    private String resolvedNotes;


}

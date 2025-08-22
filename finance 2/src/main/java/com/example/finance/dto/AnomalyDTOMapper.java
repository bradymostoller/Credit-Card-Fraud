package com.example.finance.dto;


import com.example.finance.entity.Anomaly;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AnomalyDTOMapper implements Function<Anomaly, AnomalyDTO> {
    @Override
    public AnomalyDTO apply(Anomaly anomaly) {
        return new AnomalyDTO(
                anomaly.getId(),
                anomaly.getUser().getId(),
                anomaly.getUser().getEmail(), // get email from User
                anomaly.getTransaction().getId(),
                anomaly.getTransaction().getDescription(), // get description from Transaction
                anomaly.getScore(),
                anomaly.getAnomalyType(),
                anomaly.getDetectedAt(),
                anomaly.getResolved()
        );

    }

}

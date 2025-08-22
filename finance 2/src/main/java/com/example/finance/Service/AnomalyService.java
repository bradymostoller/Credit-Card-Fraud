package com.example.finance.Service;

import com.example.finance.dto.AnomalyDTO;
import com.example.finance.dto.AnomalyDTOMapper;
import com.example.finance.entity.Anomaly;
import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import com.example.finance.repository.AnomalyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnomalyService {

    private final AnomalyRepository anomalyRepository;
    private final AnomalyDTOMapper anomalyDTOMapper;



    public List<AnomalyDTO> getAnomalies() {
        return anomalyRepository.findAll()
                .stream()
                .map(anomalyDTOMapper::apply)
                .toList();
    }

    public AnomalyDTO getAnomalyById(Long id) {
        Anomaly anomaly = anomalyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Anomaly not found with id: " + id));
        return anomalyDTOMapper.apply(anomaly);
    }

    public List<AnomalyDTO> getAnomaliesByUser(User user){
        return anomalyRepository.findByUser(user)
                .stream()
                .map(anomalyDTOMapper::apply)
                .toList();
    }

    public List<AnomalyDTO> getAnomaliesByTransaction(Transaction transaction){
        return anomalyRepository.findByTransaction(transaction)
                .stream()
                .map(anomalyDTOMapper::apply)
                .toList();
    }

}

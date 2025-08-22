package com.example.finance.repository;

import com.example.finance.dto.AnomalyDTO;
import com.example.finance.entity.Anomaly;
import com.example.finance.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.finance.entity.User;
import java.util.List;

import java.util.Optional;

public interface AnomalyRepository extends JpaRepository<Anomaly, Integer> {
    List<Anomaly> findByUser(User user);
    Optional<Anomaly> findById(Long id);
    List<Anomaly> findByTransaction(Transaction transaction);
}



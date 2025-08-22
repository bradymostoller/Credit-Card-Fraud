package com.example.finance.repository;


import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySender(User sender);

    List<Transaction> findByReceiver(User receiver);

    List<Transaction> findBySenderAndReceiver(User sender, User receiver);
}

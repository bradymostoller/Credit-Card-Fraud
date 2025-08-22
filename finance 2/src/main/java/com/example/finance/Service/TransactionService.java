package com.example.finance.Service;

import com.example.finance.Service.FraudDetectionService;
import com.example.finance.dto.TransactionRequest;
import com.example.finance.dto.TransactionResponse;
import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final FraudDetectionService fraudDetectionService;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User sender = userRepository.findByEmail(request.getSenderEmail())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        BigDecimal oldBalanceSender = sender.getBalance();
        BigDecimal oldBalanceReceiver = receiver.getBalance();
        BigDecimal amount = request.getAmount();

        // Validate sender has sufficient balance
        if (oldBalanceSender.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        BigDecimal newBalanceSender = oldBalanceSender.subtract(amount);
        BigDecimal newBalanceReceiver = oldBalanceReceiver.add(amount);

        // Perform fraud detection before processing the transaction
        FraudDetectionService.FraudPredictionResponse fraudResult = fraudDetectionService.checkFraud(
                request.getType().name(),
                amount,
                oldBalanceSender,
                newBalanceSender,
                oldBalanceReceiver,
                newBalanceReceiver
        );

        log.info("Fraud detection result for transaction: is_fraud={}, probability={}, confidence={}",
                fraudResult.isFraud(), fraudResult.getFraudProbability(), fraudResult.getConfidence());

        // Handle fraud detection result
        boolean isFraudulent = fraudResult.isFraud();
        boolean isHighRisk = fraudResult.getFraudProbability() > 0.7; // Configurable threshold

        // You can decide how to handle fraudulent transactions:
        // Option 1: Block the transaction completely
        if (isFraudulent && fraudResult.getFraudProbability() > 0.9) {
            throw new RuntimeException("Transaction blocked due to fraud detection");
        }

        // Option 2: Flag for manual review but allow transaction
        boolean requiresManualReview = isFraudulent || isHighRisk;

        // Update balances
        sender.setBalance(newBalanceSender);
        receiver.setBalance(newBalanceReceiver);

        userRepository.save(sender);
        userRepository.save(receiver);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.getAmount())
                .timestamp(request.getTimestamp())
                .description(request.getDescription())
                .type(request.getType())
                .oldBalanceOrg(oldBalanceSender)
                .newBalanceOrig(newBalanceSender)
                .oldBalanceDest(oldBalanceReceiver)
                .newBalanceDest(newBalanceReceiver)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);


        // Build response with fraud detection results
        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .senderEmail(savedTransaction.getSender().getEmail())
                .receiverEmail(savedTransaction.getReceiver().getEmail())
                .amount(savedTransaction.getAmount())
                .timestamp(savedTransaction.getTimestamp())
                .description(savedTransaction.getDescription())
                .type(savedTransaction.getType())
                .oldBalanceOrg(savedTransaction.getOldBalanceOrg())
                .newBalanceOrig(savedTransaction.getNewBalanceOrig())
                .oldBalanceDest(savedTransaction.getOldBalanceDest())
                .newBalanceDest(savedTransaction.getNewBalanceDest())
                .isFraudSuspected(isFraudulent)
                .fraudProbability(fraudResult.getFraudProbability())
                .fraudDetectionError(fraudResult.getError())
                .build();
    }

    public FraudDetectionService.FraudPredictionResponse checkTransactionForFraud(TransactionRequest request) {
        // This method allows you to check for fraud without creating the transaction
        User sender = userRepository.findByEmail(request.getSenderEmail())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        BigDecimal oldBalanceSender = sender.getBalance();
        BigDecimal oldBalanceReceiver = receiver.getBalance();
        BigDecimal amount = request.getAmount();

        BigDecimal newBalanceSender = oldBalanceSender.subtract(amount);
        BigDecimal newBalanceReceiver = oldBalanceReceiver.add(amount);

        return fraudDetectionService.checkFraud(
                request.getType().name(),
                amount,
                oldBalanceSender,
                newBalanceSender,
                oldBalanceReceiver,
                newBalanceReceiver
        );
    }
}
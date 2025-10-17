package com.payments.bankservafricaadapter.service;

import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.bankservafricaadapter.domain.BankservAfricaIso8583Message;
import com.payments.bankservafricaadapter.repository.BankservAfricaAdapterRepository;
import com.payments.bankservafricaadapter.repository.BankservAfricaIso8583MessageRepository;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for BankservAfrica ISO 8583 message processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankservAfricaIso8583ProcessingService {
    
    private final BankservAfricaAdapterRepository adapterRepository;
    private final BankservAfricaIso8583MessageRepository iso8583MessageRepository;
    
    /**
     * Process ISO 8583 message
     */
    @Transactional
    public BankservAfricaIso8583Message processIso8583Message(
            ClearingAdapterId adapterId,
            String transactionId,
            String messageType,
            String direction,
            String mti,
            String processingCode) {
        
        log.info("Processing ISO 8583 message: {} for adapter: {}", transactionId, adapterId);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
        
        if (!adapter.isActive()) {
            throw new IllegalStateException("Adapter is not active: " + adapterId);
        }
        
        // Create ISO 8583 message
        BankservAfricaIso8583Message message = BankservAfricaIso8583Message.create(
                ClearingMessageId.generate(),
                adapterId,
                transactionId,
                messageType,
                direction,
                mti,
                processingCode);
        
        // Mark as processing
        message.markAsProcessing();
        
        // Save message
        BankservAfricaIso8583Message savedMessage = iso8583MessageRepository.save(message);
        
        // Add to adapter
        adapter.addIso8583Message(savedMessage);
        adapterRepository.save(adapter);
        
        log.info("Successfully processed ISO 8583 message: {} with transaction ID: {}", message.getId(), transactionId);
        
        return savedMessage;
    }
    
    /**
     * Update ISO 8583 message with transaction details
     */
    @Transactional
    public BankservAfricaIso8583Message updateIso8583MessageDetails(
            ClearingMessageId messageId,
            BigDecimal amount,
            String currencyCode,
            String cardNumber,
            String merchantId,
            String terminalId) {
        
        log.info("Updating ISO 8583 message details: {}", messageId);
        
        BankservAfricaIso8583Message message = iso8583MessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("ISO 8583 message not found: " + messageId));
        
        message.updateTransactionDetails(amount, currencyCode, cardNumber, merchantId, terminalId);
        
        BankservAfricaIso8583Message updatedMessage = iso8583MessageRepository.save(message);
        
        log.info("Successfully updated ISO 8583 message details: {}", messageId);
        
        return updatedMessage;
    }
    
    /**
     * Update ISO 8583 message status
     */
    @Transactional
    public BankservAfricaIso8583Message updateIso8583MessageStatus(
            ClearingMessageId messageId,
            String status,
            String responseCode) {
        
        log.info("Updating ISO 8583 message status: {} to {}", messageId, status);
        
        BankservAfricaIso8583Message message = iso8583MessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("ISO 8583 message not found: " + messageId));
        
        message.updateStatus(status, responseCode);
        
        BankservAfricaIso8583Message updatedMessage = iso8583MessageRepository.save(message);
        
        log.info("Successfully updated ISO 8583 message status: {}", messageId);
        
        return updatedMessage;
    }
    
    /**
     * Set raw message
     */
    @Transactional
    public BankservAfricaIso8583Message setRawMessage(ClearingMessageId messageId, String rawMessage) {
        log.info("Setting raw message for ISO 8583 message: {}", messageId);
        
        BankservAfricaIso8583Message message = iso8583MessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("ISO 8583 message not found: " + messageId));
        
        message.setRawMessage(rawMessage);
        
        BankservAfricaIso8583Message updatedMessage = iso8583MessageRepository.save(message);
        
        log.info("Successfully set raw message for ISO 8583 message: {}", messageId);
        
        return updatedMessage;
    }
    
    /**
     * Get ISO 8583 message by ID
     */
    public Optional<BankservAfricaIso8583Message> getIso8583Message(ClearingMessageId messageId) {
        return iso8583MessageRepository.findById(messageId);
    }
    
    /**
     * Get ISO 8583 message by transaction ID
     */
    public Optional<BankservAfricaIso8583Message> getIso8583MessageByTransactionId(String transactionId) {
        BankservAfricaIso8583Message message = iso8583MessageRepository.findByTransactionId(transactionId);
        return Optional.ofNullable(message);
    }
    
    /**
     * Get ISO 8583 messages by adapter ID
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByAdapterId(ClearingAdapterId adapterId) {
        return iso8583MessageRepository.findByAdapterId(adapterId.toString());
    }
    
    /**
     * Get ISO 8583 messages by status
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByStatus(String status) {
        return iso8583MessageRepository.findByStatus(status);
    }
    
    /**
     * Get ISO 8583 messages by direction
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByDirection(String direction) {
        return iso8583MessageRepository.findByDirection(direction);
    }
    
    /**
     * Get ISO 8583 messages by MTI
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByMti(String mti) {
        return iso8583MessageRepository.findByMti(mti);
    }
    
    /**
     * Get ISO 8583 messages by processing code
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByProcessingCode(String processingCode) {
        return iso8583MessageRepository.findByProcessingCode(processingCode);
    }
    
    /**
     * Get ISO 8583 messages by response code
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByResponseCode(String responseCode) {
        return iso8583MessageRepository.findByResponseCode(responseCode);
    }
    
    /**
     * Get ISO 8583 messages by merchant ID
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByMerchantId(String merchantId) {
        return iso8583MessageRepository.findByMerchantId(merchantId);
    }
    
    /**
     * Get ISO 8583 messages by terminal ID
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByTerminalId(String terminalId) {
        return iso8583MessageRepository.findByTerminalId(terminalId);
    }
    
    /**
     * Get ISO 8583 messages by card number
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByCardNumber(String cardNumber) {
        return iso8583MessageRepository.findByCardNumber(cardNumber);
    }
    
    /**
     * Get ISO 8583 messages by amount range
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return iso8583MessageRepository.findByAmountBetween(minAmount, maxAmount);
    }
    
    /**
     * Get ISO 8583 messages by currency code
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesByCurrencyCode(String currencyCode) {
        return iso8583MessageRepository.findByCurrencyCode(currencyCode);
    }
    
    /**
     * Get ISO 8583 messages created after timestamp
     */
    public List<BankservAfricaIso8583Message> getIso8583MessagesCreatedAfter(java.time.Instant timestamp) {
        return iso8583MessageRepository.findByCreatedAtAfter(timestamp);
    }
    
    /**
     * Count ISO 8583 messages by status
     */
    public long countIso8583MessagesByStatus(String status) {
        return iso8583MessageRepository.countByStatus(status);
    }
    
    /**
     * Count ISO 8583 messages by direction
     */
    public long countIso8583MessagesByDirection(String direction) {
        return iso8583MessageRepository.countByDirection(direction);
    }
    
    /**
     * Count ISO 8583 messages by MTI
     */
    public long countIso8583MessagesByMti(String mti) {
        return iso8583MessageRepository.countByMti(mti);
    }
    
    /**
     * Count ISO 8583 messages by processing code
     */
    public long countIso8583MessagesByProcessingCode(String processingCode) {
        return iso8583MessageRepository.countByProcessingCode(processingCode);
    }
    
    /**
     * Count ISO 8583 messages by response code
     */
    public long countIso8583MessagesByResponseCode(String responseCode) {
        return iso8583MessageRepository.countByResponseCode(responseCode);
    }
    
    /**
     * Count ISO 8583 messages by merchant ID
     */
    public long countIso8583MessagesByMerchantId(String merchantId) {
        return iso8583MessageRepository.countByMerchantId(merchantId);
    }
    
    /**
     * Count ISO 8583 messages by terminal ID
     */
    public long countIso8583MessagesByTerminalId(String terminalId) {
        return iso8583MessageRepository.countByTerminalId(terminalId);
    }
    
    /**
     * Count ISO 8583 messages by card number
     */
    public long countIso8583MessagesByCardNumber(String cardNumber) {
        return iso8583MessageRepository.countByCardNumber(cardNumber);
    }
    
    /**
     * Count ISO 8583 messages by currency code
     */
    public long countIso8583MessagesByCurrencyCode(String currencyCode) {
        return iso8583MessageRepository.countByCurrencyCode(currencyCode);
    }
}

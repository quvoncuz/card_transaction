package card.uz.service;

import card.uz.dto.TransactionDTO;
import card.uz.dto.WithdrawFundDTO;
import card.uz.entity.CardEntity;
import card.uz.entity.IdempotencyKeyEntity;
import card.uz.entity.TransactionEntity;
import card.uz.enums.Currency;
import card.uz.enums.Status;
import card.uz.repository.CardRepository;
import card.uz.repository.IdempotencyKeyRepository;
import card.uz.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity<?> withdraw(WithdrawFundDTO dto, String cardId, String idempotencyKey) {
        Optional<CardEntity> optionalCard = cardRepository.findById(cardId);
        if (optionalCard.isEmpty()) {
            return ResponseEntity.status(404).body("Card not found");
        }

        String requestHash = IdempotencyService.calculateHash(dto+cardId);

        Optional<IdempotencyKeyEntity> existingKey = idempotencyKeyRepository.findById(idempotencyKey);
        if (existingKey.isPresent()) {
            IdempotencyKeyEntity idempotencyKeyEntity = existingKey.get();
            if (!idempotencyKeyEntity.getRequestHash().equals(requestHash)) {
                return ResponseEntity.status(400).body("Idempotency key conflict: request data does not match previous request.");
            }
            TransactionEntity transaction = objectMapper.readValue(idempotencyKeyEntity.getResponseBody(), TransactionEntity.class);
            return ResponseEntity.status(200).body(toDTO(transaction));
        }

        CardEntity card = optionalCard.get();

        if (card.getStatus() != Status.ACTIVE) {
            return ResponseEntity.status(400).body("Card not active");
        }
        long balance = 0L;
        Long currency = null;
        if (!card.getCurrency().equals(dto.getCurrency())){
            if (card.getCurrency().equals(Currency.USD)){
                currency = Long.parseLong(CbuCurrencyService.getByCurrencyCode("USD").getRate());
                balance = card.getBalance() - dto.getAmount()/currency;
            } else {
                currency = Long.parseLong(CbuCurrencyService.getByCurrencyCode("UZS").getRate());
                balance = card.getBalance() - dto.getAmount()*currency;
            }
        }

        if (balance < 0) {
            return ResponseEntity.status(400).body("Not Enough Balance");
        }

        card.setBalance(balance);
        cardRepository.save(card);
        TransactionEntity transaction = new TransactionEntity();
        transaction.setExternalId(dto.getExternalId());
        transaction.setCardId(cardId);
        transaction.setAmount(dto.getAmount());
        transaction.setAfterBalance(card.getBalance());
        transaction.setCurrency(dto.getCurrency());
        transaction.setPurpose(dto.getPurpose());
        transaction.setExchangeRate(currency);
        transactionRepository.save(transaction);

        IdempotencyKeyEntity idempotencyKeyEntity = new IdempotencyKeyEntity();
        idempotencyKeyEntity.setKey(idempotencyKey);
        idempotencyKeyEntity.setResponseStatus(200);
        idempotencyKeyEntity.setExpirationDate(LocalDateTime.now().plusHours(12));
        idempotencyKeyEntity.setEndpoint("/api/v1/cards/" + cardId + "/debit");
        idempotencyKeyEntity.setRequestHash(IdempotencyService.calculateHash(dto+cardId));
        idempotencyKeyEntity.setResponseBody(objectMapper.writeValueAsString(transaction));
        idempotencyKeyRepository.save(idempotencyKeyEntity);

        return ResponseEntity.ok("Withdrawal successful. New balance: " + card.getBalance());
    }

    private TransactionDTO toDTO(TransactionEntity transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(transaction.getId());
        dto.setCardId(transaction.getCardId());
        dto.setExternalId(transaction.getExternalId());
        dto.setAfterBalance(transaction.getAfterBalance());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setPurpose(transaction.getPurpose());
        return dto;
    }
}

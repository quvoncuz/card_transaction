package card.uz.service;

import card.uz.dto.*;
import card.uz.entity.CardEntity;
import card.uz.entity.IdempotencyKeyEntity;
import card.uz.entity.TransactionEntity;
import card.uz.enums.Currency;
import card.uz.enums.Purpose;
import card.uz.enums.Status;
import card.uz.enums.TransactionType;
import card.uz.exp.AppException;
import card.uz.repository.CardRepository;
import card.uz.repository.CustomFilterRepository;
import card.uz.repository.IdempotencyKeyRepository;
import card.uz.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private CustomFilterRepository customFilterRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public PageImpl<TransactionDTO> getTransactionHistory(FilterDTO dto, String cardId, int page, int size, String transactionType) {
        PageRequest pageRequest = PageRequest.of(page, size);
        FilterResultDTO<Object[]> filter = customFilterRepository.filter(dto, cardId, page, size, TransactionType.valueOf(transactionType));
        List<TransactionDTO> resultList = filter.getContent().stream().map(objects -> {
            TransactionDTO transactionDTO = new TransactionDTO();
            transactionDTO.setTransactionId((String) objects[0]);
            transactionDTO.setExternalId((String) objects[1]);
            transactionDTO.setCardId((String) objects[2]);
            transactionDTO.setAfterBalance((Long) objects[3]);
            transactionDTO.setAmount((Long) objects[4]);
            transactionDTO.setCurrency((Currency) objects[5]);
            transactionDTO.setExchangeRate((Double) objects[6]);
            transactionDTO.setPurpose((Purpose) objects[7]);
            return transactionDTO;
        }).toList();

        return new PageImpl<>(resultList, pageRequest, filter.getTotalCount());
    }

    @Transactional
    public ResponseEntity<?> withdraw(WithdrawFundDTO dto, String cardId, String idempotencyKey) {
        CardEntity card = cardRepository.findById(cardId).orElseThrow(() -> new AppException("Card Not FOund"));

        String requestHash = IdempotencyService.calculateHash(dto.toString()+ " " + cardId);

        Optional<IdempotencyKeyEntity> existingKey = idempotencyKeyRepository.findById(idempotencyKey);
        if (existingKey.isPresent()) {
            IdempotencyKeyEntity idempotencyKeyEntity = existingKey.get();
            if (!idempotencyKeyEntity.getRequestHash().equals(requestHash)) {
                return ResponseEntity.status(400).body("Idempotency key conflict: request data does not match previous request.");
            }
            TransactionEntity transaction = objectMapper.readValue(idempotencyKeyEntity.getResponseBody(), TransactionEntity.class);
            return ResponseEntity.status(200).body(toDTO(transaction));
        }

        if (card.getStatus() != Status.ACTIVE) {
            throw new AppException("Card Not Active");
        }
        long balance = 0L;
        Long currency = null;
        if (!card.getCurrency().equals(dto.getCurrency())){
            if (card.getCurrency().equals(Currency.USD)){
                currency = new BigDecimal(CbuCurrencyService.getByCurrencyCode("USD").getRate()).longValue();
                balance = card.getBalance() - dto.getAmount()/currency;
            } else {
                currency = new BigDecimal(CbuCurrencyService.getByCurrencyCode("UZS").getRate()).longValue();
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
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setExchangeRate(currency);
        transactionRepository.save(transaction);

        IdempotencyKeyEntity idempotencyKeyEntity = new IdempotencyKeyEntity();
        idempotencyKeyEntity.setKey(idempotencyKey);
        idempotencyKeyEntity.setResponseStatus(200);
        idempotencyKeyEntity.setExpirationDate(LocalDateTime.now().plusHours(12));
        idempotencyKeyEntity.setEndpoint("/api/v1/cards/" + cardId + "/debit");
        idempotencyKeyEntity.setRequestHash(IdempotencyService.calculateHash(dto.toString()+ " " + cardId));
        idempotencyKeyEntity.setResponseBody(objectMapper.writeValueAsString(transaction));
        idempotencyKeyRepository.save(idempotencyKeyEntity);

        return ResponseEntity.ok(toDTO(transaction));
    }

    @Transactional
    public ResponseEntity<?> topUp(TopUpFundDTO dto, String cardId, String idempotencyKey){
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException("Card Not Found"));

        String requestHash = IdempotencyService.calculateHash(dto.toString() + " " + cardId);

        Optional<IdempotencyKeyEntity> existingKey = idempotencyKeyRepository.findById(idempotencyKey);
        if (existingKey.isPresent()){
            IdempotencyKeyEntity entity = existingKey.get();
            if (!entity.getRequestHash().equals(requestHash)) {
                return ResponseEntity.status(400).body("Idempotency key conflict: request data does not match previous request.");
            }
            TransactionEntity transaction = objectMapper.readValue(entity.getResponseBody(), TransactionEntity.class);
            return ResponseEntity.status(200).body(toDTO(transaction));
        }

        if (card.getStatus() != Status.ACTIVE) {
            throw new AppException("Card Not Active");
        }
        Long currency = null;
        Long balance = null;
        if (!card.getCurrency().equals(dto.getCurrency())){
            if (card.getCurrency().equals(Currency.USD)){
                currency = new BigDecimal(CbuCurrencyService.getByCurrencyCode("USD").getRate()).longValue();
                card.setBalance(card.getBalance() + dto.getAmount()/currency);
            } else {
                currency = new BigDecimal(CbuCurrencyService.getByCurrencyCode("UZS").getRate()).longValue();
                card.setBalance(card.getBalance() + dto.getAmount()*currency);
            }
        }

        cardRepository.save(card);
        TransactionEntity transaction = new TransactionEntity();
        transaction.setExternalId(dto.getExternalId());
        transaction.setCardId(cardId);
        transaction.setAmount(dto.getAmount());
        transaction.setAfterBalance(card.getBalance());
        transaction.setCurrency(dto.getCurrency());
        transaction.setPurpose(null);
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setExchangeRate(currency);
        transactionRepository.save(transaction);

        IdempotencyKeyEntity idempotencyKeyEntity = new IdempotencyKeyEntity();
        idempotencyKeyEntity.setKey(idempotencyKey);
        idempotencyKeyEntity.setResponseStatus(200);
        idempotencyKeyEntity.setExpirationDate(LocalDateTime.now().plusHours(12));
        idempotencyKeyEntity.setEndpoint("/api/v1/cards/" + cardId + "/credit");
        idempotencyKeyEntity.setRequestHash(IdempotencyService.calculateHash(dto.toString()+ " " + cardId));
        idempotencyKeyEntity.setResponseBody(objectMapper.writeValueAsString(transaction));
        idempotencyKeyRepository.save(idempotencyKeyEntity);

        return ResponseEntity.ok(toDTO(transaction));
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

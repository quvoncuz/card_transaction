package card.uz.service;

import card.uz.dto.CardDTO;
import card.uz.dto.CardRequestDTO;
import card.uz.dto.CardResponseDTO;
import card.uz.entity.CardEntity;
import card.uz.entity.IdempotencyKeyEntity;
import card.uz.enums.Status;
import card.uz.repository.CardRepository;
import card.uz.repository.IdempotencyKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity<?> createCard(String indempotencyKey, CardRequestDTO requestDTO) {
        Optional<IdempotencyKeyEntity> existingKey = idempotencyKeyRepository.findById(indempotencyKey);

        String requestHash = IdempotencyService.calculateHash(requestDTO.toString());

        if (existingKey.isPresent()) {
            IdempotencyKeyEntity idempotencyKeyEntity = existingKey.get();

            if (!idempotencyKeyEntity.getRequestHash().equals(requestHash)) {
                return ResponseEntity.status(400).body("Idempotency key conflict: request data does not match previous request.");
            }

            CardEntity entity = objectMapper.readValue(idempotencyKeyEntity.getResponseBody(), CardEntity.class);
            return ResponseEntity.status(200).body(toDTO(entity));
        }

        long cardCount = cardRepository.countByUserIdAndStatusIsNot(requestDTO.getUserId(), Status.CLOSED);
        if (cardCount >= 3) {
            return ResponseEntity.status(400).body("User already has maximum number of active cards.");
        }

        CardEntity card = new CardEntity();
        card.setUserId(requestDTO.getUserId());
        card.setStatus(requestDTO.getStatus());
        card.setBalance(requestDTO.getInitialAmount());
        card.setCurrency(requestDTO.getCurrency());
        card = cardRepository.save(card);

        IdempotencyKeyEntity idempotencyKeyEntity = new IdempotencyKeyEntity();
        idempotencyKeyEntity.setKey(indempotencyKey);
        idempotencyKeyEntity.setEndpoint("/api/v1/cards");
        idempotencyKeyEntity.setRequestHash(IdempotencyService.calculateHash(requestDTO.toString()));
        idempotencyKeyEntity.setResponseStatus(201);
        idempotencyKeyEntity.setResponseBody(objectMapper.writeValueAsString(card));
        idempotencyKeyEntity.setExpirationDate(LocalDateTime.now().plusHours(12));
        idempotencyKeyRepository.save(idempotencyKeyEntity);

        CardResponseDTO responseDTO = new CardResponseDTO();
        responseDTO.setUserId(card.getUserId());
        responseDTO.setBalance(card.getBalance());
        responseDTO.setCurrency(card.getCurrency());
        responseDTO.setStatus(card.getStatus());

        return ResponseEntity.status(201).body(responseDTO);
    }

    public ResponseEntity<CardDTO> getCardById(String cardId) {
        Optional<CardEntity> optionalCard = cardRepository.findById(cardId);
        if (optionalCard.isPresent()) {
            CardDTO dto = toDTO(optionalCard.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<?> blockCard(String cardId) {
        Optional<CardEntity> optionalCard = cardRepository.findById(cardId);
        if (optionalCard.isPresent()) {
            CardEntity card = optionalCard.get();
            if (card.getStatus().equals(Status.ACTIVE)){
                card.setStatus(Status.BLOCKED);
                cardRepository.save(card);
            }
            return ResponseEntity.status(204).body("No Content");
        } else {
            return ResponseEntity.status(404).body("Card with such id not exists in processing");
        }
    }

    public ResponseEntity<?> unblockCard(String cardId) {
        Optional<CardEntity> optionalCard = cardRepository.findById(cardId);
        if (optionalCard.isPresent()) {
            CardEntity card = optionalCard.get();
            if (card.getStatus().equals(Status.BLOCKED)){
                card.setStatus(Status.ACTIVE);
                cardRepository.save(card);
            }
            return ResponseEntity.status(204).body("No Content");
        } else {
            return ResponseEntity.status(404).body("Card with such id not exists in processing.");
        }
    }

    private CardDTO toDTO(CardEntity entity) {
        CardDTO dto = new CardDTO();
        dto.setCardId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setStatus(entity.getStatus());
        dto.setBalance(entity.getBalance());
        dto.setCurrency(entity.getCurrency());
        return dto;
    }
}

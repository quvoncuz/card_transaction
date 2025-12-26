package card.uz.service;

import card.uz.dto.CardRequestDTO;
import card.uz.entity.IdempotencyKeyEntity;
import card.uz.exp.AppException;
import card.uz.repository.IdempotencyKeyRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Service
public class IdempotencyService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdempotencyKeyRepository repository;

    @Transactional
    public ResponseEntity<?> handleIdempotency(
            String key,
            String endpoint,
            CardRequestDTO request,
            Supplier<ResponseEntity<?>> operation) {

        String requestHash = calculateHash(request);

        // 1. Bir marta SELECT
        Optional<IdempotencyKeyEntity> existingOpt = repository.findById(key);

        if (existingOpt.isPresent()) {
            IdempotencyKeyEntity existing = existingOpt.get();

            // Request hash tekshirish
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new IllegalArgumentException(
                        "Idempotency key already used with different request"
                );
            }

            // Agar response mavjud bo'lsa - qaytarish
            if (existing.getResponseBody() != null) {
                try {
                    Object body = objectMapper.readValue(
                            existing.getResponseBody(),
                            Object.class
                    );
                    return ResponseEntity
                            .status(existing.getResponseStatus())
                            .body(body);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse cached response", e);
                }
            }

            // Processing holatida
            throw new AppException(
                    "Request is being processed"
            );
        }

        // 2. Yangi key yaratish
        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.setKey(key);
        entity.setRequestHash(requestHash);
        entity.setEndpoint(endpoint);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setExpirationDate(LocalDateTime.now().plusHours(24));

        // Birinchi save - response bo'sh
        repository.save(entity);

        // 3. Operation bajarish
        ResponseEntity<?> response = operation.get();

        // 4. Response ni yangilash - entity objectidan foydalanish
        try {
            entity.setResponseBody(objectMapper.writeValueAsString(response.getBody()));
            entity.setResponseStatus(response.getStatusCode().value());
            repository.save(entity);  // Bu yerda entity allaqachon managed
        } catch (Exception e) {
            log.error("Failed to save idempotency response", e);
        }

        return response;
    }

    public String calculateHash(Object request) {
        try {
            String jsonRequest = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(jsonRequest.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}

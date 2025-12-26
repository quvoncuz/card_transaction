package card.uz.controller;

import card.uz.dto.*;
import card.uz.service.CardService;
import card.uz.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
    @Autowired
    private CardService cardService;

    @Autowired
    private TransactionService transactionService;

    @PostMapping({"/", ""})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCard(@RequestHeader(value = "Idempotency-Key", required = true) String idemKey,
                                        @RequestBody CardRequestDTO dto) {
        log.info("Create card request received with Idempotency-Key: {}", idemKey);
        return cardService.createCard(idemKey, dto);
    }

    @GetMapping("/{cardId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCardDetails(@PathVariable String cardId) {
        log.info("Get card details request received for cardId: {}", cardId);
        return cardService.getCardById(cardId);
    }

    @PostMapping("/{cardId}/block")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public  ResponseEntity<?> blockCard(@PathVariable String cardId) {
        log.info("Block card request received for cardId: {}", cardId);
        return cardService.blockCard(cardId);
    }

    @PostMapping("/{cardId}/unblock")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public  ResponseEntity<?> unblockCard(@PathVariable String cardId) {
        log.info("Unblock card request received for cardId: {}", cardId);
        return cardService.unblockCard(cardId);
    }

    @PostMapping("/{cardId}/debit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> withdrawFunds(@RequestBody WithdrawFundDTO dto,
                                           @PathVariable String cardId,
                                           @RequestHeader(value = "Idempotency-Key", required = true) String idemKey){
        log.info("Withdraw funds request received");
        return transactionService.withdraw(dto, cardId, idemKey);
    }

    @PostMapping("/{cardId}/credit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> topUpFunds(@RequestBody TopUpFundDTO dto,
                                           @PathVariable String cardId,
                                           @RequestHeader(value = "Idempotency-Key", required = true) String idemKey){
        log.info("Top-up funds request received");
        return transactionService.topUp(dto, cardId, idemKey);
    }

    @PostMapping("/{cardId}/transactions")
    @PreAuthorize("hasRole('USER')")
    public PageImpl<TransactionDTO> transactions(@RequestBody FilterDTO dto,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @PathVariable String cardId,
                                                 @RequestParam String transactionType){
        return transactionService.getTransactionHistory(dto, cardId, page-1, size, transactionType);
    }
}

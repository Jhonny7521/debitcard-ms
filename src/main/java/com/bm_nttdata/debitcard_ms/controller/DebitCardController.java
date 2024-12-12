package com.bm_nttdata.debitcard_ms.controller;

import com.bm_nttdata.debitcard_ms.api.DebitCardApi;
import com.bm_nttdata.debitcard_ms.entity.DebitCard;
import com.bm_nttdata.debitcard_ms.mapper.DebitCardMapper;
import com.bm_nttdata.debitcard_ms.model.AccountAssociationRequestDto;
import com.bm_nttdata.debitcard_ms.model.BalanceResponseDto;
import com.bm_nttdata.debitcard_ms.model.DebitCardRequestDto;
import com.bm_nttdata.debitcard_ms.model.DebitCardResponseDto;
import com.bm_nttdata.debitcard_ms.model.PrimaryAccountRequestDto;
import com.bm_nttdata.debitcard_ms.model.TransactionRequestDto;
import com.bm_nttdata.debitcard_ms.model.TransactionResponseDto;
import com.bm_nttdata.debitcard_ms.service.DebitCardService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para la gestión de tarjetas de débito.
 * Esta clase implementa la interfaz DebitCardApi,
 * generada automáticamente por OpenAPI Generator, para proporcionar
 * los endpoints definidos en la especificación OpenAPI.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class DebitCardController implements DebitCardApi {

    private final DebitCardService debitCardService;

    @Override
    public Mono<ResponseEntity<DebitCardResponseDto>> getDebitCard(
            String cardId,
            ServerWebExchange exchange) {
        log.info("Getting debit card : {}", cardId);
        return debitCardService.getDebitCard(cardId)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Retrieved debit card: {}", cardId))
                ;
    }

    @Override
    @CircuitBreaker(
            name = "getDebitCardsByCustomer", fallbackMethod = "getDebitCardsByCustomerFallback")
    public Mono<ResponseEntity<Flux<DebitCardResponseDto>>> getDebitCardsByCustomer(
            String customerId,
            ServerWebExchange exchange) {
        log.info("Getting debit cards for customer: {}", customerId);
        return Mono.just(ResponseEntity.ok(
                debitCardService.getDebitCardsByCustomer(customerId)
                        .doOnComplete(() ->
                                log.info("Retrieved debit cards for customer: {}", customerId))
        ));
    }

    @Override
    @CircuitBreaker(name = "createDebitCard", fallbackMethod = "createDebitCardFallback")
    public Mono<ResponseEntity<DebitCardResponseDto>> createDebitCard(
            Mono<DebitCardRequestDto> debitCardRequestDto,
            ServerWebExchange exchange) {
        log.info("Creating debit card: {}", debitCardRequestDto);
        return debitCardService.createDebitCard(debitCardRequestDto)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Debit card created successfully"));
    }

    @Override
    @CircuitBreaker(name = "associateAccount", fallbackMethod = "associateAccountFallback")
    public Mono<ResponseEntity<DebitCardResponseDto>> associateAccount(
            String cardId,
            Mono<AccountAssociationRequestDto> accountAssociationRequestDto,
            ServerWebExchange exchange) {
        log.info("Associating account to debit card: {}", cardId);
        return debitCardService.associateAccount(cardId, accountAssociationRequestDto)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Account association successful"));
    }

    @Override
    @CircuitBreaker(
            name = "getPrimaryAccountBalance", fallbackMethod = "getPrimaryAccountBalanceFallback")
    public Mono<ResponseEntity<BalanceResponseDto>> getPrimaryAccountBalance(
            String cardId,
            ServerWebExchange exchange) {
        log.info("Getting balance for debit card: {}", cardId);
        return debitCardService.getPrimaryAccountBalance(cardId)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Retrieved balance for card: {}", cardId));
    }

    @Override
    @CircuitBreaker(name = "updatePrimaryAccount", fallbackMethod = "updatePrimaryAccountFallback")
    public Mono<ResponseEntity<DebitCardResponseDto>> updatePrimaryAccount(
            String cardId,
            Mono<PrimaryAccountRequestDto> primaryAccountRequestDto,
            ServerWebExchange exchange) {
        log.info("Updating credit card: {}", cardId);
        return debitCardService.updatePrimaryAccount(cardId, primaryAccountRequestDto)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Primary account updated for card: {}", cardId));
    }

    private Mono<ResponseEntity<String>> createDebitCardFallback(
            Mono<DebitCardRequestDto> debitCardRequestDto,
            ServerWebExchange exchange,
            Exception e) {
        log.error("Fallback: Error creating debit card: {}", e.getMessage());
        return Mono.just(ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("We are experiencing some errors. Please try again later"));
    }

    private Mono<ResponseEntity<DebitCardResponseDto>> associateAccountFallback(
            String cardId,
            Mono<AccountAssociationRequestDto> accountAssociationRequestDto,
            Exception e) {
        log.error(
                "Fallback: Error sssociating account to debit cards {}: {}",
                cardId, e.getMessage());
        return Mono.just(
                new ResponseEntity(
                        "We are experiencing some errors. Please try again later",
                        HttpStatus.SERVICE_UNAVAILABLE));
    }

    private Mono<ResponseEntity<BalanceResponseDto>> getPrimaryAccountBalanceFallback(
            String cardId,
            Exception e) {
        log.error(
                "Fallback: Error when obtaining debit card balance {}: {}",
                cardId, e.getMessage());
        return Mono.just(
                new ResponseEntity(
                        "We are experiencing some errors. Please try again later",
                        HttpStatus.SERVICE_UNAVAILABLE));
    }

    private Mono<ResponseEntity<DebitCardResponseDto>> updatePrimaryAccountFallback(
            String cardId,
            Mono<PrimaryAccountRequestDto> primaryAccountRequestDto,
            Exception e) {
        log.error(
                "Fallback: Error updating primary account to debit card {}: {}",
                cardId, e.getMessage());
        return Mono.just(
                new ResponseEntity(
                        "We are experiencing some errors. Please try again later",
                        HttpStatus.SERVICE_UNAVAILABLE));
    }

    private Mono<ResponseEntity<Flux<DebitCardResponseDto>>> getDebitCardsByCustomerFallback(
            String customerId, Exception e) {
        log.error(
                "Fallback: Error getting debit cards for customer {}: {}",
                customerId, e.getMessage());
        return Mono.just(
                new ResponseEntity(
                        "We are experiencing some errors. Please try again later",
                        HttpStatus.SERVICE_UNAVAILABLE));
    }
}

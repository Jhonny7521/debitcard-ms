package com.bm_nttdata.debitcard_ms.service.impl;

import com.bm_nttdata.debitcard_ms.dto.AccountDto;
import com.bm_nttdata.debitcard_ms.dto.CustomerDto;
import com.bm_nttdata.debitcard_ms.entity.DebitCard;
import com.bm_nttdata.debitcard_ms.entity.enums.CardStatusEnum;
import com.bm_nttdata.debitcard_ms.exception.*;
import com.bm_nttdata.debitcard_ms.mapper.DebitCardMapper;
import com.bm_nttdata.debitcard_ms.model.AccountAssociationRequestDto;
import com.bm_nttdata.debitcard_ms.model.BalanceResponseDto;
import com.bm_nttdata.debitcard_ms.model.DebitCardRequestDto;
import com.bm_nttdata.debitcard_ms.model.DebitCardResponseDto;
import com.bm_nttdata.debitcard_ms.model.PrimaryAccountRequestDto;
import com.bm_nttdata.debitcard_ms.repository.DebitCardRepository;
import com.bm_nttdata.debitcard_ms.service.DebitCardService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que gestiona las operaciones de negocio relacionadas con
 * las tarjetas de débito.
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DebitCardServiceImpl implements DebitCardService {

    private final DebitCardRepository debitCardRepository;

    private final DebitCardMapper debitCardMapper;

    private final WebClient.Builder webClient;

    /**
     * Obtiene los datos de una tarjeta de débito por su Identificador único.
     *
     * @param cardId Identificador único de tarjeta de débito
     * @return Un Mono que contiene la tarjeta de débito si se encuentra,
     *      o un Mono vacío si no existe
     */
    @Override
    public Mono<DebitCardResponseDto> getDebitCard(String cardId) {
        Mono<DebitCardResponseDto> debitCard = debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(
                        new DebitCardNotFoundException(
                                "Debit card not found with id: " + cardId)))
                .map(debitCardMapper::debitCardEntityToDebitCardDto)
                .doOnSuccess(card -> log.info("Retrieved debit card: {}", cardId));
        return debitCard;
    }

    /**
     * Recupera todas las tarjetas de débito asociadas a un cliente específico.
     *
     * @param customerId Identificador único de cliente
     * @return Un Flux que emite la secuencia de tarjetas de débito pertenecientes al cliente
     */
    @Override
    public Flux<DebitCardResponseDto> getDebitCardsByCustomer(String customerId) {
        Flux<DebitCardResponseDto> debitCard = debitCardRepository.findAllByCustomerId(customerId)
                .switchIfEmpty(Flux.empty())
                .map(debitCardMapper::debitCardEntityToDebitCardDto)
                ;
        return debitCard;
    }

    /**
     * Crea una nueva tarjeta de débito basada en la información proporcionada.
     *
     * @param debitCardRequest DTO que contiene la información necesaria para crear la tarjeta
     * @return Un Mono que contiene la tarjeta de débito creada
     */
    @Override
    public Mono<DebitCardResponseDto> createDebitCard(Mono<DebitCardRequestDto> debitCardRequest) {

        return debitCardRequest
                .switchIfEmpty(
                        Mono.error(new ApiInvalidRequestException("Customer ID is required")))
                .flatMap(request ->
                        validateCustomerAndAccount(request)
                                .then(checkCustomerDebts(request.getCustomerId()))
                        .flatMap(hasDebts -> {
                                if (hasDebts) {
                                        return Mono.error(
                                                new BusinessRuleException(
                                                        "Customer has overdue debts"));
                                }

                                LocalDate expDate = LocalDate.now().plusMonths(36);
                                YearMonth month = YearMonth.from(expDate);
                                Random random = new Random();

                                DebitCard debitCard =
                                        debitCardMapper
                                                .debitCardRequestToDebitCardEntity(request);
                                debitCard.setCardNumber(generateCardNumber());
                                debitCard.setCardPin(String.valueOf(random.nextInt(10000)));
                                debitCard.setCcvCode(String.valueOf(random.nextInt(1000)));
                                debitCard.setCreationDate(LocalDateTime.now());
                                debitCard.setExpirationDate(String.valueOf(month));
                                debitCard.setActive(CardStatusEnum.ACTIVE);
                                return debitCardRepository.save(debitCard);
                        }))
                .map(debitCardMapper::debitCardEntityToDebitCardDto);
    }

    /**
     * Asocia una cuenta bancaria a una tarjeta de débito existente.
     *
     * @param cardId El identificador único de la tarjeta de débito
     * @param accountAssociationRequestDto DTO con la información de la cuenta a asociar
     * @return Un Mono que contiene la tarjeta de débito actualizada con la nueva asociación
     */
    @Override
    public Mono<DebitCardResponseDto> associateAccount(
            String cardId, Mono<AccountAssociationRequestDto> accountAssociationRequestDto) {

        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new DebitCardNotFoundException("Debit card not found")))
                .flatMap(card ->
                        accountAssociationRequestDto
                                .switchIfEmpty(Mono.error(
                                    new BusinessRuleException(
                                            "Account Association Request is required")))
                                .flatMap(requestDto ->
                                    webClient.build()
                                        .get()
                                        .uri("http://account-ms/api/v1/accounts/"
                                                + requestDto.getAccountId())
                                        .retrieve()
                                        .bodyToMono(AccountDto.class)
                                        .switchIfEmpty(
                                                Mono.error(
                                                        new AccountNotFoundException(
                                                                "Account to associate not found")))
                                        .flatMap(account -> {
                                            if (card.getAssociatedAccountIds() == null) {
                                                card.setAssociatedAccountIds(new ArrayList<>());
                                            }
                                            if (!card.getAssociatedAccountIds()
                                                    .contains(requestDto.getAccountId())) {
                                                card.getAssociatedAccountIds()
                                                        .add(requestDto.getAccountId());
                                                return debitCardRepository.save(card);
                                            }
                                            return Mono.just(card);
                                        }))
                )
                .map(debitCardMapper::debitCardEntityToDebitCardDto)
                ;

    }

    /**
     * Consulta el saldo de la cuenta principal asociada a una tarjeta de débito.
     *
     * @param cardId El identificador único de la tarjeta de débito
     * @return Un Mono que contiene el DTO con la información del saldo de la cuenta principal
     */
    @Override
    public Mono<BalanceResponseDto> getPrimaryAccountBalance(String cardId) {
        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new DebitCardNotFoundException("Debit card not found")))
                .flatMap(card ->
                        webClient.build()
                                .get()
                                .uri("http://account-ms/api/v1/accounts/"
                                        + card.getPrimaryAccountId() + "/balance")
                                .retrieve()
                                .bodyToMono(BalanceResponseDto.class));
    }

    /**
     * Actualiza la cuenta principal de una tarjeta de débito basada en la información
     * proporcionada.
     *
     * @param cardId El identificador único de la tarjeta de débito
     * @param primaryAccountRequestDto DTO con la información de la cuenta a actualizar
     * @return Un Mono que contiene la tarjeta de débito actualizada con la nueva asociación
     */
    @Override
    public Mono<DebitCardResponseDto> updatePrimaryAccount(
            String cardId, Mono<PrimaryAccountRequestDto> primaryAccountRequestDto) {
        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new DebitCardNotFoundException("Debit card not found")))
                .flatMap(card ->
                        primaryAccountRequestDto
                            .switchIfEmpty(Mono.error(
                                new BusinessRuleException(
                                        "Account Association Request is required")))
                            .flatMap(requestDto ->
                                webClient.build()
                                    .get()
                                    .uri("http://account-ms/api/v1/accounts/"
                                            + requestDto.getAccountId())
                                    .retrieve()
                                    .bodyToMono(AccountDto.class)
                                    .switchIfEmpty(
                                            Mono.error(
                                                    new AccountNotFoundException(
                                                            "Account to associate not found")))
                                    .flatMap(account -> {
                                        card.setPrimaryAccountId(account.getId());

                                        return debitCardRepository.save(card);
                                    }))
                )
                .map(debitCardMapper::debitCardEntityToDebitCardDto);
    }

    private String generateCardNumber() {
        Random random = new Random();
        String cardNumber = String.format("%04d-%04d-%04d-%04d",
                random.nextInt(10000),
                random.nextInt(10000),
                random.nextInt(10000),
                random.nextInt(10000));
        return cardNumber;
    }

    private Mono<Void> validateCustomerAndAccount(DebitCardRequestDto request) {
        Mono<CustomerDto> customerValidation = webClient.build()
                .get()
                .uri("http://customer-ms/api/v1/customers/" + request.getCustomerId())
                .retrieve()
                .bodyToMono(CustomerDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e ->
                        Mono.error(new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId())))
                .onErrorResume(e -> {
                    log.error("Error calling customer service: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Customer Service Unavailable"));
                });

        Mono<AccountDto> accountValidation = webClient.build()
                .get()
                .uri("http://account-ms/api/v1/accounts/" + request.getPrimaryAccountId())
                .retrieve()
                .bodyToMono(AccountDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, e ->
                        Mono.error(new ResourceNotFoundException("Account not found with ID: " + request.getPrimaryAccountId())))
                .onErrorResume(e -> {
                    log.error("Error calling account service: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Account Service Unavailable"));
                });

        return Mono.zip(customerValidation, accountValidation)
                .then();
    }
    private Mono<Boolean> checkCustomerDebts(String customerId) {
        Mono<Boolean> creditDebts = webClient.build()
                .get()
                .uri("http://credit-ms/api/v1/credits/customer/" + customerId + "/debts")
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(e -> {
                    log.error("Error calling credit-ms service for credits: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Credit Service Unavailable"));
                });

        Mono<Boolean> creditCardDebts = webClient.build()
                .get()
                .uri("http://credit-ms/api/v1/credit-cards/customer/" + customerId + "/debts")
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(e -> {
                    log.error("Error calling credit-ms service for credit cards: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Credit Card Service Unavailable"));
                });

        return Mono.zip(creditDebts, creditCardDebts)
                .map(tuple -> tuple.getT1() || tuple.getT2());
    }
}

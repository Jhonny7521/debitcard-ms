package com.bm_nttdata.debitcard_ms.service;

import com.bm_nttdata.debitcard_ms.model.AccountAssociationRequestDto;
import com.bm_nttdata.debitcard_ms.model.BalanceResponseDto;
import com.bm_nttdata.debitcard_ms.model.DebitCardRequestDto;
import com.bm_nttdata.debitcard_ms.model.DebitCardResponseDto;
import com.bm_nttdata.debitcard_ms.model.PrimaryAccountRequestDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que gestiona las operaciones de negocio relacionadas con
 * las tarjetas de débito.
 */
public interface DebitCardService {

    /**
     * Obtiene los datos de una tarjeta de débito por su Identificador único.
     *
     * @param cardId Identificador único de tarjeta de débito
     * @return Un Mono que contiene la tarjeta de débito si se encuentra,
     *      o un Mono vacío si no existe
     */
    Mono<DebitCardResponseDto> getDebitCard(String cardId);

    /**
     * Recupera todas las tarjetas de débito asociadas a un cliente específico.
     *
     * @param customerId Identificador único de cliente
     * @return Un Flux que emite la secuencia de tarjetas de débito pertenecientes al cliente
     */
    Flux<DebitCardResponseDto> getDebitCardsByCustomer(String customerId);

    /**
     * Crea una nueva tarjeta de débito basada en la información proporcionada.
     *
     * @param debitCardRequest DTO que contiene la información necesaria para crear la tarjeta
     * @return Un Mono que contiene la tarjeta de débito creada
     */
    Mono<DebitCardResponseDto> createDebitCard(Mono<DebitCardRequestDto> debitCardRequest);

    /**
     * Asocia una cuenta bancaria a una tarjeta de débito existente.
     *
     * @param cardId El identificador único de la tarjeta de débito
     * @param accountAssociationRequestDto DTO con la información de la cuenta a asociar
     * @return Un Mono que contiene la tarjeta de débito actualizada con la nueva asociación
     */
    Mono<DebitCardResponseDto> associateAccount(
            String cardId, Mono<AccountAssociationRequestDto> accountAssociationRequestDto);

    /**
     * Consulta el saldo de la cuenta principal asociada a una tarjeta de débito.
     *
     * @param cardId El identificador único de la tarjeta de débito
     * @return Un Mono que contiene el DTO con la información del saldo de la cuenta principal
     */
    Mono<BalanceResponseDto> getPrimaryAccountBalance(String cardId);

    /**
     * Actualiza la cuenta principal de una tarjeta de débito basada en la información
     * proporcionada.
     *
     * @param cardId El identificador único de la tarjeta de débito
     * @param primaryAccountRequestDto DTO con la información de la cuenta a actualizar
     * @return Un Mono que contiene la tarjeta de débito actualizada con la nueva asociación
     */
    Mono<DebitCardResponseDto> updatePrimaryAccount(
            String cardId, Mono<PrimaryAccountRequestDto> primaryAccountRequestDto);
}

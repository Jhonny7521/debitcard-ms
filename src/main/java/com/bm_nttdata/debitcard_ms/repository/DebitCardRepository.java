package com.bm_nttdata.debitcard_ms.repository;

import com.bm_nttdata.debitcard_ms.entity.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio para la gestión de entidades DebitCard en MongoDB de manera reactiva.
 */
public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard, String> {

    Mono<DebitCard> findById(String id);

    Flux<DebitCard> findAllByCustomerId(String customerId);

}

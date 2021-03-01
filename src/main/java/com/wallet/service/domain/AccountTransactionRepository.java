package com.wallet.service.domain;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountTransactionRepository extends ReactiveCrudRepository<AccountTransactionEntity, UUID> {

	@Modifying
	@Query("INSERT INTO account_transaction (id, account_id, amount) VALUES (:id, :accountId, :balance)")
	Mono<Integer> create(@Param("id") UUID id, @Param("accountId") UUID accountId, @Param("balance") BigDecimal balance);

	Flux<AccountTransactionEntity> findTransactionsByAccountId(UUID accountId);
}

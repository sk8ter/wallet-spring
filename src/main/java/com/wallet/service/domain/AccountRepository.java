package com.wallet.service.domain;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountRepository extends ReactiveCrudRepository<AccountEntity, UUID> {

	@Modifying
	@Query("INSERT INTO account (id, balance) VALUES (:id, 0)")
	Mono<Integer> create(@Param("id") UUID accountId);

	@Modifying
	@Query("UPDATE account SET balance = balance + :amount WHERE id = :id")
	Mono<Integer> updateBalance(@Param("id") UUID accountId, BigDecimal amount);
}

package com.wallet.service.service;

import com.wallet.service.domain.AccountTransactionEntity;
import com.wallet.service.domain.AccountTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class AccountTransactionService {

	private final AccountService accountService;
	private final AccountTransactionRepository accountTransactionRepository;

	public AccountTransactionService(AccountService accountService, AccountTransactionRepository accountTransactionRepository) {
		this.accountService = accountService;
		this.accountTransactionRepository = accountTransactionRepository;
	}

	public Mono<AccountTransactionEntity> findById(UUID transactionId) {
		return accountTransactionRepository.findById(transactionId);
	}

	public Flux<AccountTransactionEntity> findTransactionsByAccountId(UUID accountId) {
		return accountTransactionRepository.findTransactionsByAccountId(accountId);
	}

	@Transactional
	public Mono<Integer> createTransaction(AccountTransactionEntity entity) {
		return accountTransactionRepository.create(entity.getId(), entity.getAccountId(), entity.getAmount())
			.then(accountService.updateBalance(entity.getAccountId(), entity.getAmount()));
	}
}

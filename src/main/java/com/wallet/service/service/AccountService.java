package com.wallet.service.service;

import com.wallet.service.domain.AccountEntity;
import com.wallet.service.domain.AccountRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	public Mono<Integer> createEmptyAccount(UUID accountId) {
		return accountRepository.create(accountId);
	}

	public Mono<AccountEntity> findById(UUID accountId) {
		return accountRepository.findById(accountId);
	}

	public Flux<AccountEntity> findAll() {
		return accountRepository.findAll();
	}

	public Mono<Integer> updateBalance(UUID accountId, BigDecimal amount) {
		return accountRepository.updateBalance(accountId, amount);
	}
}

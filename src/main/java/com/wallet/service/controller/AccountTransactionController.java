package com.wallet.service.controller;

import com.wallet.service.domain.AccountTransactionEntity;
import com.wallet.service.dto.AccountTransactionDto;
import com.wallet.service.service.AccountTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Manages account transactions
 */
@RestController
@RequestMapping("accounts/{accountId}/transactions")
public class AccountTransactionController {

	private final AccountTransactionService accountTransactionService;

	public AccountTransactionController(AccountTransactionService accountTransactionService) {
		this.accountTransactionService = accountTransactionService;
	}

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<AccountTransactionEntity> createTransaction(
			@PathVariable("accountId") UUID accountId,
			@RequestBody @Valid AccountTransactionDto accountTransaction
	) {
		AccountTransactionEntity entity = new AccountTransactionEntity();
		entity.setAccountId(accountId);
		entity.setId(accountTransaction.getId());
		entity.setAmount(accountTransaction.getAmount());

		return accountTransactionService.createTransaction(entity)
			.then(accountTransactionService.findById(accountTransaction.getId()));
	}

	@GetMapping()
	public Flux<AccountTransactionEntity> getTransactions(@PathVariable("accountId") UUID accountId) {
		return accountTransactionService.findTransactionsByAccountId(accountId);
	}
}

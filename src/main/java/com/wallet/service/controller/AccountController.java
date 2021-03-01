package com.wallet.service.controller;

import com.wallet.service.dto.AccountDto;
import com.wallet.service.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Manages accounts
 */
@RestController
@RequestMapping("accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<AccountDto> createAccount() {
		UUID accountId = UUID.randomUUID();
		return accountService.createEmptyAccount(accountId)
			.then(getAccount(accountId));
	}

	@GetMapping
	public Flux<AccountDto> getAccounts() {
		return accountService.findAll().map(AccountDto::new);
	}

	@GetMapping("{accountId}")
	public Mono<AccountDto> getAccount(@PathVariable("accountId") UUID id) {
		return accountService.findById(id).map(AccountDto::new);
	}
}

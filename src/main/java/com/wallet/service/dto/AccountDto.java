package com.wallet.service.dto;

import com.wallet.service.domain.AccountEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A monetary account that holds the current balance of a payer.
 */
public class AccountDto {

	private UUID id;
	private LocalDateTime created;
	private BigDecimal balance;

	public AccountDto() {
	}

	public AccountDto(AccountEntity entity) {
		this.id = entity.getId();
		this.created = entity.getCreated();
		this.balance = entity.getBalance();
	}

	/**
	 * @return account id
	 */
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * @return account creation date
	 */
	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	/**
	 * @return current balance
	 */
	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
}

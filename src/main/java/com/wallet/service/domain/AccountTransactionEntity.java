package com.wallet.service.domain;

import com.wallet.service.dto.validation.Money;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction that modifies balance on the {@link AccountEntity}.
 */
@Table("account_transaction")
public class AccountTransactionEntity {

	@Id
	@NotNull
	private UUID id;

	private LocalDateTime created;
	private UUID accountId;

	@NotNull
	@Money
	private BigDecimal amount;

	/**
	 * @return transaction id
	 */
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * @return transaction registration date
	 */
	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	/**
	 * @return account id
	 */
	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	/**
	 * @return amount of funds that was added (credit, positive) or removed (debit, negative) from account
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}

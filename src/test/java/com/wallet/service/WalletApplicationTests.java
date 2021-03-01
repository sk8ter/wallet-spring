package com.wallet.service;

import com.wallet.service.dto.AccountTransactionDto;
import com.wallet.service.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WalletApplicationTests {
	private static final String ACCOUNTS_URL = "/accounts";
	private static final String ACCOUNT_URL = ACCOUNTS_URL + "/{accountId}";
	private static final String ACCOUNT_TRANSACTIONS_URL = ACCOUNT_URL + "/transactions";

	@DynamicPropertySource
	static void postgresProperties(DynamicPropertyRegistry registry) {
		PostgresContainer.init(registry);
	}

	@BeforeEach
	public void setUp() throws IOException, InterruptedException {
		PostgresContainer.prepare();
	}

	@Autowired
	private WebTestClient webClient;

	@Autowired
	private AccountService accountService;

	@Test
	void shouldCreateEmptyAccount() {
		webClient.post().uri(ACCOUNTS_URL).contentType(MediaType.APPLICATION_JSON).exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.created").isNotEmpty()
			.jsonPath("$.balance").isEqualTo(0);
	}

	@Test
	void shouldGetAccounts() {
		UUID accountId1 = setupAccount();
		UUID accountId2 = setupAccount();

		webClient.get().uri(ACCOUNTS_URL).exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.size()").isEqualTo(2)
			.jsonPath("$.[*].created").isNotEmpty()
			.jsonPath("$.[0].id").isEqualTo(accountId1.toString())
			.jsonPath("$.[0].balance").isEqualTo(0)
			.jsonPath("$.[1].id").isEqualTo(accountId2.toString())
			.jsonPath("$.[1].balance").isEqualTo(0);
	}

	@Test
	void shouldGetAccountById() {
		UUID accountId = setupAccount(100.0);

		getAccount(accountId)
			.jsonPath("$.id").isEqualTo(accountId.toString())
			.jsonPath("$.created").isNotEmpty()
			.jsonPath("$.balance").isEqualTo(100);
	}

	@Test
	void shouldCreateTransaction() {
		UUID accountId = setupAccount();

		UUID transactionId = UUID.randomUUID();
		webClient.post().uri(ACCOUNT_TRANSACTIONS_URL, accountId)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(buildTransaction(transactionId, accountId, BigDecimal.valueOf(150)))
			.exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.id").isEqualTo(transactionId.toString())
			.jsonPath("$.created").isNotEmpty()
			.jsonPath("$.accountId").isEqualTo(accountId.toString())
			.jsonPath("$.amount").isEqualTo(150);
	}

	@Test
	void shouldGetTransactions() {
		// Given
		UUID accountId = setupAccount();

		UUID firstTransactionId = UUID.randomUUID();
		createTransactionExpectCreated(firstTransactionId, accountId, 150);

		UUID secondTransactionId = UUID.randomUUID();
		createTransactionExpectCreated(secondTransactionId, accountId, -100);

		// When
		webClient.get().uri(ACCOUNT_TRANSACTIONS_URL, accountId).exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.size()").isEqualTo(2)
			.jsonPath("$.[0].id").isEqualTo(firstTransactionId.toString())
			.jsonPath("$.[0].accountId").isEqualTo(accountId.toString())
			.jsonPath("$.[0].amount").isEqualTo(150)
			.jsonPath("$.[1].id").isEqualTo(secondTransactionId.toString())
			.jsonPath("$.[1].accountId").isEqualTo(accountId.toString())
			.jsonPath("$.[1].amount").isEqualTo(-100)
			.jsonPath("$.[*].created").isNotEmpty();
	}

	@Test
	void transactionShouldAddFundsToAccount() {
		// Given
		UUID accountId = setupAccount(150.45);

		// When
		createTransactionExpectCreated(UUID.randomUUID(), accountId, 50.43);

		// Then
		getAccount(accountId).jsonPath("$.balance").isEqualTo(200.88);
	}

	@Test
	void transactionShouldWithdrawFundsFromAccount() {
		// Given
		UUID accountId = setupAccount(150.45);

		// When
		createTransactionExpectCreated(UUID.randomUUID(), accountId, -50.40);

		// Then
		getAccount(accountId).jsonPath("$.balance").isEqualTo(100.05);
	}

	@Test
	void transactionShouldWithdrawWholeAccountBalance() {
		// Given
		UUID accountId = setupAccount(150.45);

		// When
		createTransactionExpectCreated(UUID.randomUUID(), accountId, -150.45);

		// Then
		getAccount(accountId).jsonPath("$.balance").isEqualTo(0);
	}

	@Test
	void transactionShouldFailIfBalanceBecomesNegative() {
		// Given
		UUID accountId = setupAccount(150.45);

		// When
		createTransaction(UUID.randomUUID(), accountId, -150.46).expectStatus().is5xxServerError();

		// Then
		getAccount(accountId).jsonPath("$.balance").isEqualTo(150.45);
	}

	@Test
	void transactionShouldFailIfAccountDoesNotExist() {
		createTransaction(UUID.randomUUID(), UUID.randomUUID(), 100).expectStatus().is5xxServerError();
	}

	@Test
	void transactionShouldFailIfInvalidMoneyScale() {
		createTransaction(UUID.randomUUID(), setupAccount(), -100.005).expectStatus().isBadRequest();
	}

	@Test
	void transactionShouldFailIfMoneyIsNull() {
		UUID accountId = setupAccount();

		webClient.post().uri(ACCOUNT_TRANSACTIONS_URL, accountId)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(buildTransaction(UUID.randomUUID(), accountId, null))
			.exchange()
			.expectStatus().isBadRequest();
	}

	private WebTestClient.BodyContentSpec getAccount(UUID accountId) {
		return webClient.get().uri(ACCOUNT_URL, accountId).exchange()
			.expectStatus().isOk()
			.expectBody();
	}

	private WebTestClient.ResponseSpec createTransactionExpectCreated(UUID transactionId, UUID accountId, double amount) {
		return createTransaction(transactionId, accountId, amount).expectStatus().isCreated();
	}

	private WebTestClient.ResponseSpec createTransaction(UUID transactionId, UUID accountId, double amount) {
		return webClient.post().uri(ACCOUNT_TRANSACTIONS_URL, accountId)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(buildTransaction(transactionId, accountId, BigDecimal.valueOf(amount)))
			.exchange();
	}

	private UUID setupAccount() {
		return setupAccount(null);
	}

	private UUID setupAccount(Double balance) {
		UUID accountId = UUID.randomUUID();
		accountService.createEmptyAccount(accountId).block();

		if (balance != null) {
			accountService.updateBalance(accountId, BigDecimal.valueOf(balance)).block();
		}
		return accountId;
	}

	private AccountTransactionDto buildTransaction(UUID transactionId, UUID accountId, BigDecimal amount) {
		AccountTransactionDto transaction = new AccountTransactionDto();
		transaction.setId(transactionId);
		transaction.setAccountId(accountId);
		transaction.setAmount(amount);
		return transaction;
	}
}

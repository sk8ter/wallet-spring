package com.wallet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;

public class PostgresContainer {

	private static final Logger LOG = LoggerFactory.getLogger(PostgresContainer.class);
	private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:12.2-alpine").withReuse(true);

	public static void init(DynamicPropertyRegistry registry) {
		startContainer();
		overrideRegistry(registry);
	}

	public static void prepare() throws IOException, InterruptedException {
		if (!hasDbDump()) {
			backup();
		} else {
			restore();
		}
	}

	private static void startContainer() {
		postgresContainer.start();
		LOG.info("Container postgres url: " + postgresContainer.getJdbcUrl());
	}

	private static void overrideRegistry(DynamicPropertyRegistry registry) {
		registry.add("spring.r2dbc.url", PostgresContainer::r2dbcUrl);
		registry.add("spring.r2dbc.username", postgresContainer::getUsername);
		registry.add("spring.r2dbc.password", postgresContainer::getPassword);

		// configure liquibase to use the same database
		registry.add("spring.liquibase.url", postgresContainer::getJdbcUrl);
		registry.add("spring.liquibase.user", postgresContainer::getUsername);
		registry.add("spring.liquibase.password", postgresContainer::getPassword);
	}

	private static boolean hasDbDump() throws IOException, InterruptedException {
		return ensureCommandExecuted(postgresContainer.execInContainer("ls")).getStdout().contains("db.dump");
	}

	private static void backup() throws IOException, InterruptedException {
		String[] command = {
				"/usr/local/bin/pg_dump",
				"--username", postgresContainer.getUsername(),
				"--dbname", postgresContainer.getDatabaseName(),
				"--format", "t",
				"--file", "db.dump"
		};
		ensureCommandExecuted(postgresContainer.execInContainer(command));
	}

	private static void restore() throws IOException, InterruptedException {
		String[] command = {
				"/usr/local/bin/pg_restore",
				"--username", postgresContainer.getUsername(),
				"--dbname", postgresContainer.getDatabaseName(),
				"--clean",
				"db.dump"
		};
		ensureCommandExecuted(postgresContainer.execInContainer(command));
	}

	private static String r2dbcUrl() {
		return String.format("r2dbc:postgresql://%s:%s/%s",
				postgresContainer.getContainerIpAddress(),
				postgresContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
				postgresContainer.getDatabaseName());
	}

	private static Container.ExecResult ensureCommandExecuted(Container.ExecResult result) {
		if (result.getExitCode() != 0) {
			throw new RuntimeException("Could not perform command in the container: " + result.getStderr());
		}
		return result;
	}
}

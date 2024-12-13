package ru.t1.java.demo.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.t1.java.demo.kafka.message.TransactionAcceptMessage;
import ru.t1.java.demo.kafka.producer.TransactionAcceptProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.OperationType;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.util.TransactionMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yml")
@WireMockTest(httpPort = 8082)
public class TransactionProcessingServiceIntegrationTest {

    @Autowired
    private TransactionProcessingService transactionProcessingService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private TransactionService transactionService;

    @Qualifier("transactionAcceptProducer")
    @MockBean
    private TransactionAcceptProducer transactionAcceptProducer;

    @Value("${web.resources.blacklist-check}")
    private String blacklistCheckUrl;

    private Account account;
    private Client client;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .clientId(UUID.randomUUID())
                .blocked(null) // Unknown status
                .build();

        account = Account.builder()
                .accountId(UUID.randomUUID())
                .balance(BigDecimal.valueOf(1000))
                .owner(client)
                .build();

        transaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .account(account)
                .amount(BigDecimal.valueOf(100))
                .operationType(OperationType.OUTGOING)
                .build();

        when(accountService.getAccount(eq(account.getAccountId()))).thenReturn(account);
        when(clientService.getClient(eq(client.getClientId()))).thenReturn(client);
        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(transaction);
    }


    @Test
    void processTransaction_shouldHandleUnknownClientStatusBlocked() {

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(blacklistCheckUrl))
                .withQueryParam("clientId", WireMock.matching(client.getClientId().toString()))
                .withQueryParam("accountId", WireMock.matching(account.getAccountId().toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"blocked\": true}")));

        transactionProcessingService.processTransaction(TransactionMapper.toDto(transaction));

        verify(accountService).changeAccountStatus(eq(account.getAccountId()), eq(AccountStatus.BLOCKED));
        verify(transactionService).changeTransactionStatus(eq(transaction.getTransactionId()), eq(TransactionStatus.REJECTED));
        verify(accountService).alterAccountBalance(eq(transaction), eq(account));

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo(blacklistCheckUrl))
                .withQueryParam("clientId", WireMock.matching(client.getClientId().toString()))
                .withQueryParam("accountId", WireMock.matching(account.getAccountId().toString())));

    }

    @Test
    void processTransaction_shouldHandleUnknownClientStatusNotBlocked() {
        doNothing().when(transactionAcceptProducer).send(any(TransactionAcceptMessage.class));

        TransactionAcceptMessage expectedMessage = TransactionAcceptMessage.builder()
                .clientId(client.getClientId())
                .accountId(account.getAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionAmount(transaction.getAmount())
                .operationType(transaction.getOperationType())
                .accountBalance(account.getBalance())
                .build();

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo(blacklistCheckUrl))
                .withQueryParam("clientId", WireMock.matching(client.getClientId().toString()))
                .withQueryParam("accountId", WireMock.matching(account.getAccountId().toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"blocked\": false}")));

        transactionProcessingService.processTransaction(TransactionMapper.toDto(transaction));

        verify(transactionAcceptProducer).send(any(TransactionAcceptMessage.class));
        verify(transactionAcceptProducer).send(argThat(message ->
                message.getClientId().equals(expectedMessage.getClientId()) &&
                        message.getAccountId().equals(expectedMessage.getAccountId()) &&
                        message.getTransactionId().equals(expectedMessage.getTransactionId()) &&
                        message.getTransactionAmount().equals(expectedMessage.getTransactionAmount()) &&
                        message.getOperationType().equals(expectedMessage.getOperationType()) &&
                        message.getAccountBalance().equals(expectedMessage.getAccountBalance())
        ));

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo(blacklistCheckUrl))
                .withQueryParam("clientId", WireMock.matching(client.getClientId().toString()))
                .withQueryParam("accountId", WireMock.matching(account.getAccountId().toString())));
    }
}


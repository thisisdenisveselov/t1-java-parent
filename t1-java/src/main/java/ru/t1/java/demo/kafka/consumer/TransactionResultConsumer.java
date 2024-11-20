package ru.t1.java.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.exception.IllegalOperationTypeException;
import ru.t1.java.demo.kafka.message.TransactionResultMessage;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.OperationType;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionService;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionResultConsumer {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @KafkaListener(groupId = "${t1.kafka.consumer.transaction-result-id}",
            topics = "${t1.kafka.topic.transaction_result}",
            containerFactory = "transactionResultKafkaListenerContainerFactory")
    public void listener(@Payload TransactionResultMessage transactionResultMessage, Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("TransactionResult consumer: new message from {} topic", topic);

        try {
            TransactionStatus status = transactionResultMessage.getStatus();
            Transaction transaction = new Transaction();

            switch (status) {
                case ACCEPTED -> {
                    transaction.setStatus(TransactionStatus.ACCEPTED);
                    transactionService.updateTransaction(transactionResultMessage.getTransactionId(), transaction);
                }
                case REJECTED -> {
                    if (transactionResultMessage.getOperationType() == OperationType.INCOMING)
                        accountService.decreaseBalance(transactionResultMessage.getAccountId(), transactionResultMessage.getTransactionAmount());
                    else if (transactionResultMessage.getOperationType() == OperationType.OUTGOING)
                        accountService.increaseBalance(transactionResultMessage.getAccountId(), transactionResultMessage.getTransactionAmount());
                    else
                        throw new IllegalOperationTypeException("Unknown operation type: " + transactionResultMessage.getOperationType());

                    transaction.setStatus(TransactionStatus.REJECTED);
                    transactionService.updateTransaction(transactionResultMessage.getTransactionId(), transaction);
                }
                case BLOCKED -> {
                    Account account = Account.builder().status(AccountStatus.BLOCKED).build();
                    accountService.updateAccount(transactionResultMessage.getAccountId(), account);

                    transaction.setStatus(TransactionStatus.BLOCKED);
                    transactionService.updateTransaction(transactionResultMessage.getTransactionId(), transaction);
                }
                default ->
                    throw new IllegalArgumentException("Unknown transaction status: " + status);
            }

        } catch (IllegalOperationTypeException illegalOperationTypeException) {
            log.error("TransactionResult consumer: unknown operation type: {}", transactionResultMessage.getOperationType(), illegalOperationTypeException);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("TransactionResult consumer: unknown translation status: {}", transactionResultMessage.getStatus(), illegalArgumentException);
        } catch (Throwable throwable) {
            log.error("TransactionResult consumer: Error while processing new message: {}", transactionResultMessage.toString(), throwable);
        } finally {
            ack.acknowledge();
        }

        log.info("TransactionResult consumer: message processed");
    }
}

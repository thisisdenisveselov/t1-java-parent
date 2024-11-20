package ru.t1.java.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.AccountDto;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.util.AccountMapper;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountConsumer {

    private final AccountService accountService;

    @KafkaListener(groupId = "${t1.kafka.consumer.account-id}",
            topics = "${t1.kafka.topic.accounts}",
            containerFactory = "accountKafkaListenerContainerFactory")
    public void listener(@Payload AccountDto accountDto, Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Account consumer: new message from {} topic", topic);
        Account account = AccountMapper.toEntity(accountDto);

        try {
            accountService.createAccount(account);
        } catch (Throwable throwable) {
            log.error("Account consumer: Error while saving new account: {}", account.toString(), throwable);
        } finally {
            ack.acknowledge();
        }

        log.info("Account consumer: message processed");
    }
}

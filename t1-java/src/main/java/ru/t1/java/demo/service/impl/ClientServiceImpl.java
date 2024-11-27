package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.kafka.producer.ClientProducer;
import ru.t1.java.demo.model.User;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.exception.EntityNotFoundException;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.service.UserService;
import ru.t1.java.demo.util.ClientMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("ClientServiceImpl")
@Slf4j
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final ClientProducer clientProducer;
    private final UserService userService;
    private final UserServiceImpl userServiceImpl;
    private final ClientRepository clientRepository;

    /*@PostConstruct
    void init() {
        List<Client> clients = new ArrayList<>();
        try {
            clients = parseJson();
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }
        if (!clients.isEmpty())
            repository.saveAll(clients);
    }*/

    @Override
//    @LogExecution
//    @Track
//    @HandlingResult
    public List<Client> parseJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ClientDto[] clients = mapper.readValue(new File("t1-java/src/main/resources/MOCK_DATA.json"), ClientDto[].class);

        return Arrays.stream(clients)
                .map(ClientMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    public Client getClient(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("%s with id = %d not found", "Client", id)));
    }

    @LogDataSourceError
    @Override
    public Client getClient(UUID clientId) {
        return repository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("%s with clientId = %s not found", "Client", clientId.toString())));
    }

    @Override
    @Transactional
    public List<Client> registerClients(List<Client> clients) {
        List<Client> savedClients = new ArrayList<>();
        for (Client client : clients) {
            Client saved = registerClient(client);
            savedClients.add(saved);
//            savedClients.add(repository.save(client));
        }

        return savedClients;
    }

    @Override
    @Transactional
    public Client registerClient(Client client) {
        UUID clientId = UUID.randomUUID();
        client.setClientId(clientId);

        Client saved = repository.save(client);
        clientProducer.send(client.getId());

        //connect User and Client
        Long id = userServiceImpl.getCurrentUserId();
        User user = User.builder().clientId(clientId).build();

        userService.updateUser(id, user);
        return saved;
    }
    @Override
    public void clearMiddleName(List<ClientDto> dtos) {
        log.info("Clearing middle name");
        dtos.forEach(dto -> dto.setMiddleName(null));
        log.info("Done clearing middle name");
    }

    @Override
    @Transactional
    public Client updateClient(UUID clientId, Client client) {
        Client updatedClient = getClient(clientId);

        if (client.getBlocked() != null)
            updatedClient.setBlocked(client.getBlocked());

        return clientRepository.save(updatedClient);
    }

}

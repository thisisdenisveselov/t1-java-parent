package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.ClientDto;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ClientService {
    List<Client> parseJson() throws IOException;
    Client getClient(Long id);

    Client getClient(UUID clientId);

    List<Client> registerClients(List<Client> clients);

    Client registerClient(Client client);

    void clearMiddleName(List<ClientDto> dtos);

    Client updateClient(UUID clientId, Client updatedClient);
}

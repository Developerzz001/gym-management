package com.gymmanagement.client;

import com.gymmanagement.client.dto.ClientRequest;
import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.common.dto.PageResponse;

public interface ClientService {

    ClientResponse registerClient(ClientRequest request);

    ClientResponse updateClient(Long id, ClientRequest request);

    ClientResponse deactivateClient(Long id);

    ClientResponse getClientById(Long id);

    ClientResponse getClientByUserId(Long userId);

    PageResponse<ClientResponse> getClients(String keyword, int page, int size);

    java.util.List<ClientResponse> getClientsByCoach(String coachEmail);

    java.util.List<ClientResponse> getClientsByDietician(String dieticianEmail);

    Client getClientEntityById(Long id);

    Client getClientEntityByUserId(Long userId);
}

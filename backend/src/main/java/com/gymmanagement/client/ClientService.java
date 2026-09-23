package com.gymmanagement.client;

import com.gymmanagement.client.dto.ClientRequest;
import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.client.dto.ClientProfileRequest;
import com.gymmanagement.common.dto.PageResponse;

public interface ClientService {

    ClientResponse registerClient(ClientRequest request);

    ClientResponse registerInquiry(ClientRequest request);

    ClientResponse updateClient(Long id, ClientRequest request);

    ClientResponse deactivateClient(Long id);

    ClientResponse activateClient(Long id);

    ClientResponse getClientById(Long id);

    ClientResponse getClientByUserId(Long userId);

    ClientResponse updateOwnProfile(Long userId, ClientProfileRequest request);

    void updateProfileImage(Long id, byte[] image, String contentType);

    PageResponse<ClientResponse> getClients(String keyword, RegistrationType registrationType, int page, int size);

    ClientResponse convertInquiry(Long id, ClientRequest request);

    java.util.List<ClientResponse> getClientsByCoach(String coachEmail);

    java.util.List<ClientResponse> getClientsByDietician(String dieticianEmail);

    Client getClientEntityById(Long id);

    Client getClientEntityByUserId(Long userId);
}

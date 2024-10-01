package com.bufalari.service;

import com.bufalari.convert.ClientConverter;
import com.bufalari.dto.AlternativeContactDTO;
import com.bufalari.dto.ClientDTO;
import com.bufalari.dto.ClientResponseDTO;
import com.bufalari.dto.GeocodingResponseDTO;
import com.bufalari.entity.AlternativeContactEntity;
import com.bufalari.entity.ClientEntity;
import com.bufalari.entity.GeoCoordinatesEntity;
import com.bufalari.exception.ClientAlreadyExistsException;
import com.bufalari.exception.ClientNotFoundException;
import com.bufalari.exception.GeocodingApiException;
import com.bufalari.exception.InvalidClientDataException;
import com.bufalari.repository.AlternativeContactRepository;
import com.bufalari.repository.ClientRepository;
import com.bufalari.repository.GeocodingClientRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing client entities.
 */
@Service
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final AlternativeContactService alternativeContactService;
    private final ClientConverter clientConverter;
    private final GeocodingClientRepository geocodingClientRepository;
    private final Validator validator;
    private final AlternativeContactRepository alternativeContactRepository;

    /**
     * Constructor for ClientService.
     *
     * @param clientRepository          The repository for accessing client data.
     * @param clientConverter           The converter for mapping between client entities and DTOs.
     * @param geocodingClientRepository The repository for accessing the Google Maps Geocoding API.
     * @param alternativeContactService The service for managing alternative contacts.
     * @param validator                 The validator for validating client data.
     */
    @Autowired
    public ClientService(ClientRepository clientRepository,
                         ClientConverter clientConverter,
                         GeocodingClientRepository geocodingClientRepository,
                         AlternativeContactRepository alternativeContactRepository,
                         AlternativeContactService alternativeContactService,
                         Validator validator) {
        this.clientRepository = clientRepository;
        this.alternativeContactRepository = alternativeContactRepository;
        this.clientConverter = clientConverter;
        this.geocodingClientRepository = geocodingClientRepository;
        this.alternativeContactService = alternativeContactService;
        this.validator = validator;
    }

    /**
     * Retrieves a list of all clients.
     *
     * @return A sorted list of ClientResponseDTOs representing all clients.
     */
    public List<ClientResponseDTO> getAllClients() {
        String traceId = generateTraceId();
        logger.info("[TRACE-ID: {}] - Starting retrieval of all clients.", traceId);

        List<ClientEntity> clients = clientRepository.findAllByDeletedFalse();
        // Use clientConverter for conversion
        List<ClientResponseDTO> clientResponseDTOs = clients.stream()
                .map(clientConverter::convertEntityToResponseDTO)
                .sorted(Comparator.comparing(ClientResponseDTO::getName))
                .collect(Collectors.toList());

        logger.info("[TRACE-ID: {}] - Retrieval of all clients completed successfully. Number of clients found: {}", traceId, clientResponseDTOs.size());
        return clientResponseDTOs;
    }

    /**
     * Retrieves a client by ID.
     *
     * @param id The ID of the client to retrieve.
     * @return A ClientResponseDTO representing the client with the given ID.
     * @throws ClientNotFoundException If no client with the given ID is found.
     */
    public ClientResponseDTO getClientById(Long id) {
        String traceId = generateTraceId();
        logger.info("[TRACE-ID: {}] - Retrieving client with ID: {}", traceId, id);

        Optional<ClientEntity> optionalClient = clientRepository.findById(id)
                .filter(client -> !client.isDeleted());

        // Use clientConverter for conversion
        ClientResponseDTO clientResponseDTO = optionalClient.map(clientConverter::convertEntityToResponseDTO)
                .orElseThrow(() -> {
                    logger.error("[TRACE-ID: {}] - Client with ID: {} not found.", traceId, id);
                    return new ClientNotFoundException("Client not found with ID: " + id);
                });

        logger.info("[TRACE-ID: {}] - Client with ID: {} retrieved successfully.", traceId, id);
        return clientResponseDTO;
    }

    /**
     * Creates a new client.
     *
     * @param clientDTO The ClientDTO representing the client to create.
     * @return A ClientResponseDTO representing the created client.
     * @throws ClientAlreadyExistsException If a client with the same email and SIN number already exists.
     * @throws InvalidClientDataException   If the provided client data is invalid.
     */
    @Transactional
    public ClientResponseDTO createClient(ClientDTO clientDTO) {
        String traceId = generateTraceId();
        logger.info("[TRACE-ID: {}] - Starting client creation: {}", traceId, clientDTO);

        // Validate client data
        validateClientDTO(clientDTO, traceId);

        if (clientRepository.findByEmailAndSinNumberAndDeletedFalse(clientDTO.getEmail(), clientDTO.getSinNumber()).isPresent()) {
            logger.error("[TRACE-ID: {}] - A client with email: {} and SIN number: {} already exists.", traceId, clientDTO.getEmail(), clientDTO.getSinNumber());
            throw new ClientAlreadyExistsException("Client with this email and SIN number already exists.");
        }

        String address = String.format("%s, %s, %s, %s",
                clientDTO.getAddress(), clientDTO.getCity(), clientDTO.getProvince(), clientDTO.getPostalCode());

        double[] coordinates = getCoordinatesFromAddress(address, traceId);
        GeoCoordinatesEntity geoCoordinates = new GeoCoordinatesEntity();
        geoCoordinates.setLatitude(coordinates[0]);
        geoCoordinates.setLongitude(coordinates[1]);

        ClientEntity clientEntity = clientConverter.convertDTOToEntity(clientDTO);
        clientEntity.setGeoCoordinates(geoCoordinates);
        ClientEntity savedClient = clientRepository.save(clientEntity);

        // Save alternative contacts using AlternativeContactService
        alternativeContactService.saveAlternativeContacts(savedClient, clientDTO.getAlternativeContacts(), traceId);

        // Save the main contact
        AlternativeContactEntity mainContactEntity = new AlternativeContactEntity();
        mainContactEntity.setClient(savedClient);
        mainContactEntity.setName(clientDTO.getName());
        mainContactEntity.setDDI(clientDTO.getDdI1());
        mainContactEntity.setEmail(clientDTO.getEmail());
        mainContactEntity.setPhoneNumber(clientDTO.getPhoneNumber1());
        mainContactEntity.setNotes("Main Contact");
        alternativeContactRepository.save(mainContactEntity);

        ClientResponseDTO clientResponseDTO = clientConverter.convertEntityToResponseDTO(savedClient);
        logger.info("[TRACE-ID: {}] - Client created successfully: {}", traceId, clientResponseDTO);
        return clientResponseDTO;
    }


    @Transactional
    public ClientResponseDTO updateClient(Long id, ClientDTO clientDTO) {
        String traceId = generateTraceId();

        // Busca o cliente existente pelo id
        ClientEntity existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client not found with id: " + id));
        // Validação adicional (se necessária)
        if (clientDTO.getName() == null || clientDTO.getName().isEmpty()) {
            throw new InvalidClientDataException("Client name is invalid");
        }

        // Atualiza os campos do cliente com os dados do DTO
        existingClient.setName(clientDTO.getName());
        existingClient.setAddress(clientDTO.getAddress());
        existingClient.setPhoneNumber1(clientDTO.getPhoneNumber1());
        existingClient.setDdI1(clientDTO.getDdI1());
        existingClient.setPhoneNumber2(clientDTO.getPhoneNumber2());
        existingClient.setDdI2(clientDTO.getDdI2());
        existingClient.setEmail(clientDTO.getEmail());
        existingClient.setProvince(clientDTO.getProvince());
        existingClient.setPostalCode(clientDTO.getPostalCode());
        existingClient.setCity(clientDTO.getCity());
        existingClient.setCountry(clientDTO.getCountry());
        existingClient.setNotes(clientDTO.getNotes());
        // Atualize outros campos conforme necessário...

        // Obtém os contatos alternativos diretamente de clientDTO
        List<AlternativeContactDTO> contactDTOs = clientDTO.getAlternativeContacts();

        if (contactDTOs != null) {
            logger.debug("[TRACE-ID: {}] - Updating {} alternative contacts for client: {}", traceId, contactDTOs.size(), existingClient.getId());

            // Usar Map para armazenar contatos existentes para consulta mais rápida
            Map<Long, AlternativeContactEntity> existingContactsMap = existingClient.getAlternativeContacts().stream()
                    .collect(Collectors.toMap(AlternativeContactEntity::getId, c -> c));

            for (AlternativeContactDTO contactDTO : contactDTOs) {
                if (contactDTO.getId() != null && existingContactsMap.containsKey(contactDTO.getId())) {
                    // Atualizar contato existente
                    AlternativeContactEntity contactEntity = existingContactsMap.get(contactDTO.getId());
                    contactEntity.setName(contactDTO.getName());
                    contactEntity.setDDI(contactDTO.getDdI());
                    contactEntity.setPhoneNumber(contactDTO.getPhoneNumber());
                    contactEntity.setEmail(contactDTO.getEmail());
                    contactEntity.setNotes(contactDTO.getNotes());
                } else {
                    // Criar novo contato se o ID não existir
                    AlternativeContactEntity newContactEntity = new AlternativeContactEntity();
                    newContactEntity.setClient(existingClient);
                    newContactEntity.setName(contactDTO.getName());
                    newContactEntity.setDDI(contactDTO.getDdI());
                    newContactEntity.setEmail(contactDTO.getEmail());
                    newContactEntity.setPhoneNumber(contactDTO.getPhoneNumber());
                    newContactEntity.setNotes(contactDTO.getNotes());
                    existingClient.getAlternativeContacts().add(newContactEntity); // Adicionar à lista do cliente
                }
            }

            // Salvar todas as alterações de contatos alternativos
            alternativeContactRepository.saveAll(existingClient.getAlternativeContacts());
        }

        // Atualizar o cliente principal
        clientRepository.save(existingClient);

        // Retornar o cliente atualizado como ClientResponseDTO
        return clientConverter.convertEntityToResponseDTO(existingClient);
    }

    /**
     * Soft deletes a client by ID, marking it as deleted in the database.
     *
     * @param id The ID of the client to soft delete.
     * @throws ClientNotFoundException If no client with the given ID is found.
     */
    @Transactional
    public void deleteClient(Long id) {
        String traceId = generateTraceId();
        logger.info("[TRACE-ID: {}] - Soft deleting client with ID: {}", traceId, id);

        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("[TRACE-ID: {}] - Client with ID: {} not found for soft deletion.", traceId, id);
                    return new ClientNotFoundException("Client not found with ID: " + id);
                });
        client.setDeleted(true);
        clientRepository.save(client);
        logger.info("[TRACE-ID: {}] - Client with ID: {} soft deleted successfully.", traceId, id);
    }

    /**
     * Activates a previously soft-deleted client by ID.
     *
     * @param id The ID of the client to activate.
     * @throws ClientNotFoundException If no client with the given ID is found.
     */
    @Transactional
    public void activateClient(Long id) {
        String traceId = generateTraceId();
        logger.info("[TRACE-ID: {}] - Activating client with ID: {}", traceId, id);

        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("[TRACE-ID: {}] - Client with ID: {} not found for activation.", traceId, id);
                    return new ClientNotFoundException("Client not found with ID: " + id);
                });
        client.setDeleted(false);
        clientRepository.save(client);
        logger.info("[TRACE-ID: {}] - Client with ID: {} activated successfully.", traceId, id);
    }

    /**
     * Searches for clients by name, returning a sorted list of matches.
     *
     * @param name The name to search for (case-insensitive).
     * @return A sorted list of ClientResponseDTOs matching the search criteria.
     */
    public List<ClientResponseDTO> searchClientsByName(String name) {
        String traceId = generateTraceId();
        logger.info("[TRACE-ID: {}] - Searching for clients by name: {}", traceId, name);

        List<ClientEntity> clients = clientRepository.findByNameContainingIgnoreCaseAndDeletedFalse(name);
        // Use clientConverter for conversion
        List<ClientResponseDTO> clientResponseDTOs = clients.stream()
                .map(clientConverter::convertEntityToResponseDTO)
                .sorted(Comparator.comparing(ClientResponseDTO::getName)) // Sorting by name (A-Z)
                .collect(Collectors.toList());

        logger.info("[TRACE-ID: {}] - Search completed successfully. {} clients found for name: {}", traceId, clientResponseDTOs.size(), name);
        return clientResponseDTOs;
    }

    /**
     * Retrieves a client by email.
     *
     * @param email The email of the client to retrieve.
     * @return A ClientResponseDTO representing the client with the given email.
     * @throws ClientNotFoundException If no client with the given email is found.
     */
    public ClientResponseDTO getClientByEmail(String email) {
        String traceId = generateTraceId();
        logger.info("[TRACE-ID: {}] - Retrieving client by email: {}", traceId, email);

        Optional<ClientEntity> optionalClient = clientRepository.findByEmailAndSinNumberAndDeletedFalse(email, "");
        // Use clientConverter for conversion
        ClientResponseDTO clientResponseDTO = optionalClient.map(clientConverter::convertEntityToResponseDTO)
                .orElseThrow(() -> {
                    logger.error("[TRACE-ID: {}] - Client with email: {} not found.", traceId, email);
                    return new ClientNotFoundException("Client not found with email: " + email);
                });

        logger.info("[TRACE-ID: {}] - Client with email: {} retrieved successfully.", traceId, email);
        return clientResponseDTO;
    }

    /**
     * Retrieves a client by SIN number.
     *
     * @param sinNumber The SIN number of the client to retrieve.
     * @return A ClientResponseDTO representing the client with the given SIN number.
     * @throws ClientNotFoundException If no client with the given SIN number is found.
     */
    public ClientResponseDTO getClientBySinNumber(String sinNumber) {
        String traceId = generateTraceId();
        logger.info("[TRACE-ID: {}] - Retrieving client by SIN number: {}", traceId, sinNumber);

        Optional<ClientEntity> optionalClient = clientRepository.findBySinNumberAndDeletedFalse(sinNumber);
        // Use clientConverter for conversion
        ClientResponseDTO clientResponseDTO = optionalClient.map(clientConverter::convertEntityToResponseDTO)
                .orElseThrow(() -> {
                    logger.error("[TRACE-ID: {}] - Client with SIN number: {} not found.", traceId, sinNumber);
                    return new ClientNotFoundException("Client not found with SIN: " + sinNumber);
                });

        logger.info("[TRACE-ID: {}] - Client with SIN number: {} retrieved successfully.", traceId, sinNumber);
        return clientResponseDTO;
    }

    /**
     * Retrieves geographic coordinates for a given address using the Google Maps Geocoding API.
     *
     * @param address The address to geocode.
     * @param traceId A unique identifier for tracking the request.
     * @return An array of doubles containing the latitude and longitude.
     * @throws GeocodingApiException If the geocoding request fails or returns an error.
     */
    public double[] getCoordinatesFromAddress(String address, String traceId) {
        try {
            logger.debug("[TRACE-ID: {}] - Getting coordinates for address: {}", traceId, address);
            String accessToken = GeocodingClientRepository.getAccessToken();
            GeocodingResponseDTO response = geocodingClientRepository.getCoordinates(address, accessToken);

            if ("OK".equals(response.getStatus())) {
                double latitude = response.getResults().get(0).getGeometry().getLocation().getLat();
                double longitude = response.getResults().get(0).getGeometry().getLocation().getLng();
                logger.debug("[TRACE-ID: {}] - Coordinates retrieved successfully. Latitude: {}, Longitude: {}", traceId, latitude, longitude);
                return new double[]{latitude, longitude};
            } else {
                String errorMessage = String.format("Geocoding API request failed with status: %s for address: %s", response.getStatus(), address);
                logger.error("[TRACE-ID: {}] - {}", traceId, errorMessage);
                throw new GeocodingApiException(errorMessage, null);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to retrieve coordinates from address: %s", address);
            logger.error("[TRACE-ID: {}] - {}. Error: {}", traceId, errorMessage, e.getMessage());
            throw new GeocodingApiException(errorMessage, e);
        }
    }

    /**
     * Validates the provided ClientDTO using Bean Validation.
     *
     * @param clientDTO The ClientDTO to validate.
     * @param traceId   A unique identifier for tracking the request.
     * @throws InvalidClientDataException If the client data is invalid.
     */
    private void validateClientDTO(ClientDTO clientDTO, String traceId) {
        var violations = validator.validate(clientDTO);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            logger.error("[TRACE-ID: {}] - Invalid client data: {}", traceId, errorMessage);
            throw new InvalidClientDataException(errorMessage);
        }
    }

    /**
     * Generates a unique trace ID for the request.
     *
     * @return A UUID string representing the trace ID.
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
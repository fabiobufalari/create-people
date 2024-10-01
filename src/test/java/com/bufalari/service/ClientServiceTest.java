package com.bufalari.service;

import com.bufalari.convert.ClientConverter;
import com.bufalari.dto.*;
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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AlternativeContactRepository alternativeContactRepository;

    @Mock
    private ClientConverter clientConverter;

    @Mock
    private GeocodingClientRepository geocodingClientRepository;

    @Mock
    private AlternativeContactService alternativeContactService; // Mock the AlternativeContactService

    @InjectMocks
    private ClientService clientService;

    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        clientService = new ClientService(clientRepository, clientConverter, geocodingClientRepository, alternativeContactRepository, alternativeContactService, validator);
    }

    @Test
    void testGetAllClients() {
        // Mock data
        ClientEntity client1 = new ClientEntity();
        client1.setId(1L);
        client1.setName("Client A");

        ClientEntity client2 = new ClientEntity();
        client2.setId(2L);
        client2.setName("Client B");

        when(clientRepository.findAllByDeletedFalse()).thenReturn(Arrays.asList(client2, client1)); // Returning unsorted
        when(clientConverter.convertEntityToResponseDTO(any(ClientEntity.class))).thenCallRealMethod();

        // Call service method
        List<ClientResponseDTO> result = clientService.getAllClients();

        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Client A", result.get(0).getName()); // Check if it's sorted
        assertEquals("Client B", result.get(1).getName());
    }

    @Test
    void testGetClientById_ExistingClient() {
        // Mock data
        ClientEntity client = new ClientEntity();
        client.setId(1L);
        client.setName("John Doe");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientConverter.convertEntityToResponseDTO(any(ClientEntity.class))).thenCallRealMethod();

        // Call service method
        ClientResponseDTO result = clientService.getClientById(1L);

        // Assertions
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetClientById_NonExistingClient() {
        // Mock data
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        // Assertions
        assertThrows(ClientNotFoundException.class, () -> clientService.getClientById(1L),
                "Should throw ClientNotFoundException when client does not exist.");
    }

    @Test
    void testCreateClient_ValidClient() {
        // Mock data
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("John Doe");
        clientDTO.setCity("Toronto");
        clientDTO.setCountry("Canada");
        clientDTO.setProvince("ON");
        clientDTO.setPostalCode("M5V 2H5");
        clientDTO.setAddress("123 Yonge St");
        clientDTO.setDdI1("+1");
        clientDTO.setPhoneNumber1("(416) 555-1212");
        clientDTO.setEmail("john.doe@example.com");
        clientDTO.setSinNumber("123456789");
        clientDTO.setNotes("Some notes.");
        // Add at least one alternative contact
        AlternativeContactDTO contactDTO = new AlternativeContactDTO();
        contactDTO.setName("Jane Doe");
        contactDTO.setDdI("+1");
        contactDTO.setPhoneNumber("(416) 111-2222");
        contactDTO.setEmail("jane.doe@example.com");
        contactDTO.setNotes("Some notes for alternative contact.");
        clientDTO.setAlternativeContacts(List.of(contactDTO));

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setId(1L);
        clientEntity.setName("John Doe");
        clientEntity.setCity("Toronto");
        clientEntity.setCountry("Canada");
        clientEntity.setProvince("ON");
        clientEntity.setPostalCode("M5V 2H5");
        clientEntity.setAddress("123 Yonge St");
        clientEntity.setDdI1("+1");
        clientEntity.setPhoneNumber1("(416) 555-1212");
        clientEntity.setEmail("john.doe@example.com");
        clientEntity.setSinNumber("123456789");
        clientEntity.setNotes("Some notes.");
        // Set geoCoordinates
        GeoCoordinatesEntity geoCoordinates = new GeoCoordinatesEntity();
        geoCoordinates.setLatitude(43.6532);
        geoCoordinates.setLongitude(-79.3832);
        clientEntity.setGeoCoordinates(geoCoordinates);

        when(geocodingClientRepository.getCoordinates(anyString(), anyString()))
                .thenReturn(createGeocodingResponseDTO());
        when(clientConverter.convertDTOToEntity(clientDTO)).thenReturn(clientEntity);
        when(clientRepository.save(clientEntity)).thenReturn(clientEntity);
        when(clientConverter.convertEntityToResponseDTO(clientEntity)).thenCallRealMethod();

        // Call service method
        ClientResponseDTO result = clientService.createClient(clientDTO);

        // Assertions
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("Toronto", result.getCity());
        assertEquals("Canada", result.getCountry());
        assertEquals("ON", result.getProvince());
        assertEquals("M5V 2H5", result.getPostalCode());
        assertEquals("123 Yonge St", result.getAddress());
        assertEquals("+1", result.getDdI1());
        assertEquals("(416) 555-1212", result.getPhoneNumber1());
        assertEquals("john.doe@example.com", result.getEmail());
        //assertEquals("123456789", result.getSinNumber());
        assertNotNull(result.getMapLink()); // Check if mapLink is not null

        // ... other assertions

        verify(alternativeContactService, times(1)).saveAlternativeContacts(any(ClientEntity.class), anyList(), anyString());
        verify(alternativeContactRepository, times(1)).save(any(AlternativeContactEntity.class)); // Verify main contact is saved
    }


    @Test
    void testCreateClient_DuplicateClient() {
        // Mock data
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("John Doe");
        clientDTO.setCity("Toronto");
        clientDTO.setCountry("Canada");
        clientDTO.setProvince("ON");
        clientDTO.setPostalCode("M5V 2H5");
        clientDTO.setAddress("123 Yonge St");
        clientDTO.setDdI1("+1");
        clientDTO.setPhoneNumber1("(416) 555-1212");
        clientDTO.setEmail("john.doe@example.com");
        clientDTO.setSinNumber("123456789");
        clientDTO.setNotes("Some notes.");

        // Add at least one alternative contact with required fields
        AlternativeContactDTO contactDTO = new AlternativeContactDTO();
        contactDTO.setName("Jane Doe");
        contactDTO.setDdI("+1");
        contactDTO.setPhoneNumber("(416) 111-2222");
        contactDTO.setEmail("jane.doe@example.com");
        contactDTO.setNotes("Some notes for alternative contact.");
        clientDTO.setAlternativeContacts(List.of(contactDTO));

        when(clientRepository.findByEmailAndSinNumberAndDeletedFalse(clientDTO.getEmail(), clientDTO.getSinNumber()))
                .thenReturn(Optional.of(new ClientEntity()));

        // Assertions
        assertThrows(ClientAlreadyExistsException.class, () -> clientService.createClient(clientDTO),
                "Should throw ClientAlreadyExistsException when client already exists.");
    }

    @Test
    void testCreateClient_InvalidClientData() {
        // Mock data
        ClientDTO clientDTO = new ClientDTO(); // Missing required fields

        // Assertions
        assertThrows(InvalidClientDataException.class, () -> clientService.createClient(clientDTO),
                "Should throw InvalidClientDataException when client data is invalid.");
    }

    @Test
    void testUpdateClient_ExistingClient() {
        String traceId = UUID.randomUUID().toString();

        // Mock do cliente existente
        ClientEntity existingClient = new ClientEntity();
        existingClient.setId(1L);
        existingClient.setName("John Doe");
        existingClient.setCity("Toronto");
        existingClient.setAlternativeContacts(new ArrayList<>()); // Lista vazia inicialmente

        // Mock dos dados atualizados do cliente
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Jane Doe");
        clientDTO.setCity("Vancouver");

        // Mock dos contatos alternativos atualizados
        AlternativeContactDTO updatedContactDTO = new AlternativeContactDTO();
        updatedContactDTO.setId(10L);
        updatedContactDTO.setName("Updated Contact Name");
        clientDTO.setAlternativeContacts(List.of(updatedContactDTO));

        // Mock do ClientResponseDTO convertido
        ClientResponseDTO responseDTO = new ClientResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Jane Doe");
        responseDTO.setCity("Vancouver");

        // Mocking dos repositórios e conversores
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(clientConverter.convertEntityToResponseDTO(existingClient)).thenReturn(responseDTO);
        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Chamada do método no serviço
        ClientResponseDTO result = clientService.updateClient(1L, clientDTO);

        // Assertivas
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("Vancouver", result.getCity());

        // Verificar contatos alternativos
        assertEquals(1, existingClient.getAlternativeContacts().size());
        AlternativeContactEntity updatedContact = existingClient.getAlternativeContacts().get(0);
        assertEquals("Updated Contact Name", updatedContact.getName());

        // Verificar que o repositório foi chamado
        verify(alternativeContactRepository, times(1)).saveAll(anyList());
        verify(clientRepository, times(1)).save(existingClient);
    }

    @Test
    void testUpdateClient_NonExistingClient() {
        // Mock dos dados do cliente
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Jane Doe");
        // Outros campos...

        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        // Assertiva
        assertThrows(ClientNotFoundException.class, () -> clientService.updateClient(1L, clientDTO),
                "Deveria lançar ClientNotFoundException quando o cliente não existe.");
    }

    @Test
    void testUpdateClient_InvalidClientData() {
        // Mock de dados inválidos (nome faltando)
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setCity("Toronto");
        clientDTO.setCountry("Canada");
        // Outros campos obrigatórios ausentes

        // Mock do cliente existente
        ClientEntity existingClient = new ClientEntity();
        existingClient.setId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));

        // Assertiva
        assertThrows(InvalidClientDataException.class, () -> clientService.updateClient(1L, clientDTO),
                "Deveria lançar InvalidClientDataException quando os dados do cliente são inválidos.");
    }


    @Test
    void testDeleteClient_ExistingClient() {
        // Mock data
        ClientEntity existingClient = new ClientEntity();
        existingClient.setId(1L);
        existingClient.setDeleted(false);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));

        // Call service method
        clientService.deleteClient(1L);

        // Assertions
        verify(clientRepository, times(1)).save(existingClient);
        assertTrue(existingClient.isDeleted());
    }

    @Test
    void testDeleteClient_NonExistingClient() {
        // Mock data
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        // Assertions
        assertThrows(ClientNotFoundException.class, () -> clientService.deleteClient(1L),
                "Should throw ClientNotFoundException when client does not exist.");
    }

    @Test
    void testActivateClient_ExistingClient() {
        // Mock data
        ClientEntity existingClient = new ClientEntity();
        existingClient.setId(1L);
        existingClient.setDeleted(true);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));

        // Call service method
        clientService.activateClient(1L);

        // Assertions
        verify(clientRepository, times(1)).save(existingClient);
        assertFalse(existingClient.isDeleted());
    }

    @Test
    void testActivateClient_NonExistingClient() {
        Long nonExistingClientId = 999L; // An ID that doesn't exist in your test data
        when(clientRepository.findById(nonExistingClientId)).thenReturn(Optional.empty());

        // Assertion: Expecting a ClientNotFoundException to be thrown
        assertThrows(ClientNotFoundException.class, () -> clientService.activateClient(nonExistingClientId));
    }

    @Test
    void testSearchClientsByName() {
        // Mock data
        ClientEntity client1 = new ClientEntity();
        client1.setId(1L);
        client1.setName("John Doe");

        ClientEntity client2 = new ClientEntity();
        client2.setId(2L);
        client2.setName("Jane Doe");

        when(clientRepository.findByNameContainingIgnoreCaseAndDeletedFalse("Doe"))
                .thenReturn(Arrays.asList(client2, client1)); // Returning unsorted
        when(clientConverter.convertEntityToResponseDTO(any(ClientEntity.class))).thenCallRealMethod();

        // Call service method
        List<ClientResponseDTO> result = clientService.searchClientsByName("Doe");

        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Jane Doe", result.get(0).getName()); // Check if it's sorted
        assertEquals("John Doe", result.get(1).getName());
    }

    @Test
    void testGetClientByEmail_ExistingClient() {
        // Mock data
        ClientEntity client = new ClientEntity();
        client.setId(1L);
        client.setEmail("john.doe@example.com");
        when(clientRepository.findByEmailAndSinNumberAndDeletedFalse("john.doe@example.com", "")).thenReturn(Optional.of(client));
        when(clientConverter.convertEntityToResponseDTO(any(ClientEntity.class))).thenCallRealMethod();

        // Call service method
        ClientResponseDTO result = clientService.getClientByEmail("john.doe@example.com");

        // Assertions
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
    }

    @Test
    void testGetClientByEmail_NonExistingClient() {
        // Mock data
        when(clientRepository.findByEmailAndSinNumberAndDeletedFalse("john.doe@example.com", "")).thenReturn(Optional.empty());

        // Assertions
        assertThrows(ClientNotFoundException.class, () -> clientService.getClientByEmail("john.doe@example.com"),
                "Should throw ClientNotFoundException when client does not exist.");
    }


    @Test
    void testGetClientBySinNumber_NonExistingClient() {
        // Mock data
        when(clientRepository.findBySinNumberAndDeletedFalse("123456789")).thenReturn(Optional.empty());

        // Assertions
        assertThrows(ClientNotFoundException.class, () -> clientService.getClientBySinNumber("123456789"),
                "Should throw ClientNotFoundException when client does not exist.");
    }


    @Test
    void testGetCoordinatesFromAddress_Success() {
        // Mock data
        String address = "123 Yonge St, Toronto, ON, M5V 2H5";
        String traceId = UUID.randomUUID().toString();
        GeocodingResponseDTO mockResponse = createGeocodingResponseDTO();
        when(geocodingClientRepository.getCoordinates(address, GeocodingClientRepository.getAccessToken())).thenReturn(mockResponse);

        // Call service method
        double[] coordinates = clientService.getCoordinatesFromAddress(address, traceId);

        // Assertions
        assertNotNull(coordinates);
        assertEquals(2, coordinates.length);
        assertEquals(43.6532, coordinates[0]);
        assertEquals(-79.3832, coordinates[1]);
    }

    @Test
    void testGetCoordinatesFromAddress_GeocodingApiError() {
        // Mock data
        String address = "Invalid Address";
        String traceId = UUID.randomUUID().toString();
        GeocodingResponseDTO mockResponse = new GeocodingResponseDTO();
        mockResponse.setStatus("ZERO_RESULTS"); // Simulate API error
        when(geocodingClientRepository.getCoordinates(address, GeocodingClientRepository.getAccessToken())).thenReturn(mockResponse);

        // Assertions
        assertThrows(GeocodingApiException.class, () -> clientService.getCoordinatesFromAddress(address, traceId));
    }

    @Test
    void testConvertToResponseDTO() {
        // Mock data
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setId(1L);
        clientEntity.setName("John Doe");
        // ... other fields ...
        GeoCoordinatesEntity geoCoordinates = new GeoCoordinatesEntity();
        geoCoordinates.setLatitude(43.6532);
        geoCoordinates.setLongitude(-79.3832);
        clientEntity.setGeoCoordinates(geoCoordinates);

        when(clientConverter.convertEntityToResponseDTO(clientEntity)).thenCallRealMethod();

        // Call service method
        ClientResponseDTO result = clientConverter.convertEntityToResponseDTO(clientEntity);

        // Assertions
        assertNotNull(result);
        assertNotNull(result.getMapLink());
        assertTrue(result.getMapLink().containsKey("googleMaps"));
        assertTrue(result.getMapLink().containsKey("waze"));
        assertTrue(result.getMapLink().containsKey("appleMaps"));
        assertTrue(result.getMapLink().containsKey("sygic"));
        assertTrue(result.getMapLink().containsKey("hereWeGo"));
    }

    // Helper method to create a mock GeocodingResponseDTO
    private GeocodingResponseDTO createGeocodingResponseDTO() {
        GeocodingResponseDTO responseDTO = new GeocodingResponseDTO();
        responseDTO.setStatus("OK");

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setLat(43.6532);
        locationDTO.setLng(-79.3832);

        GeometryDTO geometryDTO = new GeometryDTO();
        geometryDTO.setLocation(locationDTO);

        GeocodingResultDTO resultDTO = new GeocodingResultDTO();
        resultDTO.setGeometry(geometryDTO);

        responseDTO.setResults(List.of(resultDTO));

        return responseDTO;
    }

    // Helper method to create a mock GeocodingResponseDTO
    private GeocodingResponseDTO createGeocodingResponseDTO(double lat, double lng) {
        GeocodingResponseDTO responseDTO = new GeocodingResponseDTO();
        responseDTO.setStatus("OK");

        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setLat(lat);
        locationDTO.setLng(lng);

        GeometryDTO geometryDTO = new GeometryDTO();
        geometryDTO.setLocation(locationDTO);

        GeocodingResultDTO resultDTO = new GeocodingResultDTO();
        resultDTO.setGeometry(geometryDTO);

        responseDTO.setResults(List.of(resultDTO));

        return responseDTO;
    }
}
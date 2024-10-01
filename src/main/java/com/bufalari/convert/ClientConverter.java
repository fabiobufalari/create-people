package com.bufalari.convert;

import com.bufalari.dto.AlternativeContactDTO;
import com.bufalari.dto.ClientDTO;
import com.bufalari.dto.ClientResponseDTO;
import com.bufalari.entity.AlternativeContactEntity;
import com.bufalari.entity.ClientEntity;
import com.bufalari.util.MapLinkGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Component responsible for converting between ClientEntity, ClientDTO, and ClientResponseDTO objects.
 */
@Component
public class ClientConverter {


    /**
     * Converts a ClientEntity to a ClientResponseDTO.
     *
     * @param clientEntity The ClientEntity to convert.
     * @return The corresponding ClientResponseDTO.
     */
    public ClientResponseDTO convertEntityToResponseDTO(ClientEntity clientEntity) {
        if (clientEntity == null) {
            return null;
        }

        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();

        // Set basic client details first
        clientResponseDTO.setId(clientEntity.getId());
        clientResponseDTO.setName(clientEntity.getName());
        clientResponseDTO.setAddress(clientEntity.getAddress());
        clientResponseDTO.setPhoneNumber1(clientEntity.getPhoneNumber1());
        clientResponseDTO.setDdI1(clientEntity.getDdI1());
        clientResponseDTO.setPhoneNumber2(clientEntity.getPhoneNumber2());
        clientResponseDTO.setDdI2(clientEntity.getDdI2());
        clientResponseDTO.setEmail(clientEntity.getEmail());
        clientResponseDTO.setProvince(clientEntity.getProvince());
        clientResponseDTO.setPostalCode(clientEntity.getPostalCode());
        clientResponseDTO.setCity(clientEntity.getCity());
        clientResponseDTO.setCountry(clientEntity.getCountry());
        clientResponseDTO.setNotes(clientEntity.getNotes());
        clientResponseDTO.setAlternativeContacts(convertAlternativeContactEntityToDTO(clientEntity.getAlternativeContacts()));

        // Generate map links based on geocoordinates
        if (clientEntity.getGeoCoordinates() != null) {
            double latitude = clientEntity.getGeoCoordinates().getLatitude();
            double longitude = clientEntity.getGeoCoordinates().getLongitude();
            clientResponseDTO.setMapLink(MapLinkGenerator.generateMapLinks(latitude, longitude));
        }

        return clientResponseDTO;
    }

    /**
     * Converts a ClientDTO to a ClientEntity.
     *
     * @param clientDTO The ClientDTO to convert.
     * @return The corresponding ClientEntity.
     */
    public ClientEntity convertDTOToEntity(ClientDTO clientDTO) {
        if (clientDTO == null) {
            return null;
        }

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setId(clientDTO.getId());
        clientEntity.setName(clientDTO.getName());
        clientEntity.setAddress(clientDTO.getAddress());
        clientEntity.setPhoneNumber1(clientDTO.getPhoneNumber1());
        clientEntity.setDdI1(clientDTO.getDdI1());
        clientEntity.setPhoneNumber2(clientDTO.getPhoneNumber2());
        clientEntity.setDdI2(clientDTO.getDdI2());
        clientEntity.setEmail(clientDTO.getEmail());
        clientEntity.setProvince(clientDTO.getProvince());
        clientEntity.setPostalCode(clientDTO.getPostalCode());
        clientEntity.setCity(clientDTO.getCity());
        clientEntity.setSinNumber(clientDTO.getSinNumber());
        // Correção: Passando o clientEntity corretamente
        clientEntity.setAlternativeContacts(convertAlternativeContactDTOToEntity(clientDTO.getAlternativeContacts(), clientEntity));

        return clientEntity;
    }

    // Helper methods to convert lists of alternative contacts

    /**
     * Converts a list of AlternativeContactEntity to a list of AlternativeContactDTO.
     *
     * @param entities The list of AlternativeContactEntity to convert.
     * @return The corresponding list of AlternativeContactDTO.
     */
    private List<AlternativeContactDTO> convertAlternativeContactEntityToDTO(List<AlternativeContactEntity> entities) {
        return entities.stream()
                .map(this::convertAlternativeContactEntityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts an AlternativeContactEntity to an AlternativeContactDTO.
     *
     * @param entity The AlternativeContactEntity to convert.
     * @return The corresponding AlternativeContactDTO.
     */
    private AlternativeContactDTO convertAlternativeContactEntityToDTO(AlternativeContactEntity entity) {
        AlternativeContactDTO dto = new AlternativeContactDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDdI(entity.getDDI());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        dto.setNotes(entity.getNotes());
        return dto;
    }

    /**
     * Converts a list of AlternativeContactDTO to a list of AlternativeContactEntity,
     * associating each contact with the provided ClientEntity.
     *
     * @param dtos   The list of AlternativeContactDTO to convert.
     * @param client The ClientEntity to associate the contacts with.
     * @return The corresponding list of AlternativeContactEntity.
     */
    private List<AlternativeContactEntity> convertAlternativeContactDTOToEntity(List<AlternativeContactDTO> dtos, ClientEntity client) {
        return dtos.stream()
                .map(dto -> convertAlternativeContactDTOToEntity(dto, client))
                .collect(Collectors.toList());
    }

    /**
     * Converts an AlternativeContactDTO to an AlternativeContactEntity,
     * associating the contact with the provided ClientEntity.
     *
     * @param dto    The AlternativeContactDTO to convert.
     * @param client The ClientEntity to associate the contact with.
     * @return The corresponding AlternativeContactEntity.
     */
    private AlternativeContactEntity convertAlternativeContactDTOToEntity(AlternativeContactDTO dto, ClientEntity client) {
        AlternativeContactEntity entity = new AlternativeContactEntity();
        entity.setClient(client); // Set the client for the alternative contact
        entity.setName(dto.getName());
        entity.setDDI(dto.getDdI());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setEmail(dto.getEmail());
        entity.setNotes(dto.getNotes());
        return entity;
    }
}
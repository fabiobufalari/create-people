package com.bufalari.people.convert;

import com.bufalari.people.dto.PersonDTO;
import com.bufalari.people.entity.GeoCoordinatesEntity;
import com.bufalari.people.entity.PersonEntity;
import com.bufalari.people.util.MapLinkGenerator;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory; // Importar LoggerFactory
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Converts between PersonEntity (with UUID ID) and PersonDTO (with UUID ID).
 * Converte entre PersonEntity (com ID UUID) e PersonDTO (com ID UUID).
 */
@Component
public class PersonConverter {

    private static final Logger log = LoggerFactory.getLogger(PersonConverter.class);

    /**
     * Converts PersonDTO to PersonEntity.
     * Note: Group, SubGroup, and GeoCoordinates need to be set separately in the service layer.
     * Converte PersonDTO para PersonEntity.
     * Nota: Group, SubGroup e GeoCoordinates precisam ser definidos separadamente na camada de serviço.
     */
    public PersonEntity dtoToEntity(PersonDTO dto) {
        if (dto == null) {
            return null;
        }
        return PersonEntity.builder()
                .id(dto.getId()) // <<<--- UUID (Mantém para updates)
                .fullName(dto.getName()) // <<<--- Mapeia 'name' do DTO para 'fullName'
                .document(dto.getDocument())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(dto.getRole())
                .age(dto.getAge())
                .address(dto.getAddress())
                .city(dto.getCity())
                .province(dto.getProvince())
                .country(dto.getCountry())
                .postalCode(dto.getPostalCode())
                .companyId(dto.getCompanyId()) // <<<--- UUID
                // Group, SubGroup e GeoCoordinates são definidos no serviço
                .deleted(false) // Garante que não está deletado ao converter do DTO
                .build();
    }

    /**
     * Converts PersonEntity to PersonDTO.
     * Populates groupName, subGroupName, and mapLinks.
     * Converte PersonEntity para PersonDTO.
     * Preenche groupName, subGroupName e mapLinks.
     */
    public PersonDTO entityToDTO(PersonEntity entity) {
        if (entity == null) {
            return null;
        }
        PersonDTO dto = PersonDTO.builder()
                .id(entity.getId()) // <<<--- UUID
                .name(entity.getFullName()) // <<<--- Mapeia 'fullName' para 'name' no DTO
                .document(entity.getDocument())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .role(entity.getRole())
                .age(entity.getAge())
                .address(entity.getAddress())
                .city(entity.getCity())
                .province(entity.getProvince())
                .country(entity.getCountry())
                .postalCode(entity.getPostalCode())
                .companyId(entity.getCompanyId()) // <<<--- UUID
                // IDs de Group/SubGroup e Nomes são preenchidos abaixo
                .build();

        // Preenche informações do Grupo
        if (entity.getGroup() != null) {
            dto.setGroupId(entity.getGroup().getId()); // <<<--- UUID
            dto.setGroupName(entity.getGroup().getName());
        }

        // Preenche informações do SubGrupo
        if (entity.getSubGroup() != null) {
            dto.setSubGroupId(entity.getSubGroup().getId()); // <<<--- UUID
            dto.setSubGroupName(entity.getSubGroup().getName());
        }

        // Preenche links de mapa se houver coordenadas
        GeoCoordinatesEntity coordinates = entity.getGeoCoordinates();
        if (coordinates != null && coordinates.getLatitude() != null && coordinates.getLongitude() != null) {
            try {
                Map<String, String> links = MapLinkGenerator.generateMapLinks(
                        coordinates.getLatitude(),
                        coordinates.getLongitude()
                );
                dto.setMapLinks(links); // Define o mapa de links
            } catch (Exception e) {
                // Loga o erro mas não impede a conversão
                log.error("Error generating map links for person ID {}: {}", entity.getId(), e.getMessage());
            }
        }

        return dto;
    }
}
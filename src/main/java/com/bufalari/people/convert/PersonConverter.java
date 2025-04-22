package com.bufalari.people.convert;

import com.bufalari.people.dto.PersonDTO;
import com.bufalari.people.entity.GeoCoordinatesEntity;
import com.bufalari.people.entity.PersonEntity;
import com.bufalari.people.util.MapLinkGenerator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Converts between PersonEntity and PersonDTO.
 */
@Component
public class PersonConverter {

    /**
     * Converts PersonDTO to PersonEntity.
     * Note: Group and SubGroup need to be set separately in the service layer.
     * GeoCoordinates are also typically handled during creation/update logic.
     */
    public PersonEntity dtoToEntity(PersonDTO dto) {
        if (dto == null) {
            return null;
        }
        return PersonEntity.builder()
                .id(dto.getId()) // Keep ID for updates
                .fullName(dto.getName()) // Map 'name' from DTO to 'fullName' in Entity
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
                .companyId(dto.getCompanyId())
                // Group, SubGroup, and GeoCoordinates are set in the service
                .build();
    }

    /**
     * Converts PersonEntity to PersonDTO.
     * Populates groupName, subGroupName, and mapLinks.
     */
    public PersonDTO entityToDTO(PersonEntity entity) {
        if (entity == null) {
            return null;
        }
        PersonDTO dto = PersonDTO.builder()
                .id(entity.getId())
                .name(entity.getFullName()) // Map 'fullName' from Entity to 'name' in DTO
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
                .companyId(entity.getCompanyId())
                .build();


        // Populate Group info
        if (entity.getGroup() != null) {
            dto.setGroupId(entity.getGroup().getId());
            dto.setGroupName(entity.getGroup().getName());
        }

        // Populate SubGroup info
        if (entity.getSubGroup() != null) {
            dto.setSubGroupId(entity.getSubGroup().getId());
            dto.setSubGroupName(entity.getSubGroup().getName());
        }

        // Populate Map Links if coordinates exist
        GeoCoordinatesEntity coordinates = entity.getGeoCoordinates();
        if (coordinates != null && coordinates.getLatitude() != null && coordinates.getLongitude() != null) {
             try {
                Map<String, String> links = MapLinkGenerator.generateMapLinks(
                        coordinates.getLatitude(),
                        coordinates.getLongitude()
                );
                dto.setMapLinks(links); // Set the map
             } catch (Exception e) {
                 // Log error if link generation fails, but don't break the conversion
                 System.err.println("Error generating map links: " + e.getMessage()); // Replace with proper logging
             }

        }

        return dto;
    }
}
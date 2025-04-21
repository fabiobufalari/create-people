package com.bufalari.createpeople.convert;

import com.bufalari.createpeople.dto.PersonDTO;
import com.bufalari.createpeople.entity.PersonEntity;
import com.bufalari.createpeople.util.MapLinkGenerator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Conversor entre PersonEntity e PersonDTO.
 * Converter between PersonEntity and PersonDTO.
 */
@Component
public class PersonConverter {

    public PersonEntity dtoToEntity(PersonDTO dto) {
        return PersonEntity.builder()
                .id(dto.getId())
                .fullName(dto.getName())
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
                .build();
    }

    public PersonDTO entityToDTO(PersonEntity entity) {
        PersonDTO dto = new PersonDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getFullName());
        dto.setDocument(entity.getDocument());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setRole(entity.getRole());
        dto.setAge(entity.getAge());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setProvince(entity.getProvince());
        dto.setCountry(entity.getCountry());
        dto.setPostalCode(entity.getPostalCode());
        dto.setCompanyId(entity.getCompanyId());

        if (entity.getGroup() != null) {
            dto.setGroupId(entity.getGroup().getId());
            dto.setGroupName(entity.getGroup().getName());
        }

        if (entity.getSubGroup() != null) {
            dto.setSubGroupId(entity.getSubGroup().getId());
            dto.setSubGroupName(entity.getSubGroup().getName());
        }

        if (entity.getGeoCoordinates() != null) {
            Double latitude = entity.getGeoCoordinates().getLatitude();
            Double longitude = entity.getGeoCoordinates().getLongitude();
            if (latitude != null && longitude != null) {
                Map<String, String> links = MapLinkGenerator.generateMapLinks(latitude, longitude);
                dto.setMapLink(links);
            }
        }

        return dto;
    }
}

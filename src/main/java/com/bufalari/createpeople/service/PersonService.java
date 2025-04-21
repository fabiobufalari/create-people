package com.bufalari.createpeople.service;

import com.bufalari.createpeople.config.GeoProperties;
import com.bufalari.createpeople.convert.PersonConverter;
import com.bufalari.createpeople.dto.GeocodingResponseDTO;
import com.bufalari.createpeople.dto.PersonDTO;
import com.bufalari.createpeople.entity.GeoCoordinatesEntity;
import com.bufalari.createpeople.entity.GroupEntity;
import com.bufalari.createpeople.entity.PersonEntity;
import com.bufalari.createpeople.entity.SubGroupEntity;
import com.bufalari.createpeople.exception.GeocodingApiException;
import com.bufalari.createpeople.exception.InvalidPersonDataException;
import com.bufalari.createpeople.exception.PersonAlreadyExistsException;
import com.bufalari.createpeople.exception.ResourceNotFoundException;
import com.bufalari.createpeople.repository.GeocodingClientRepository;
import com.bufalari.createpeople.repository.GroupRepository;
import com.bufalari.createpeople.repository.PersonRepository;
import com.bufalari.createpeople.repository.SubGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço responsável por operações relacionadas à entidade Person.
 * Service responsible for operations related to the Person entity.
 */
@Service
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonConverter personConverter;
    private final GroupRepository groupRepository;
    private final SubGroupRepository subGroupRepository;
    private final GeocodingClientRepository geocodingClient;
    private final GeoProperties geoProperties;

    public PersonService(
            PersonRepository personRepository,
            PersonConverter personConverter,
            GroupRepository groupRepository,
            SubGroupRepository subGroupRepository,
            GeocodingClientRepository geocodingClient,
            GeoProperties geoProperties) {
        this.personRepository = personRepository;
        this.personConverter = personConverter;
        this.groupRepository = groupRepository;
        this.subGroupRepository = subGroupRepository;
        this.geocodingClient = geocodingClient;
        this.geoProperties = geoProperties;
    }

    public PersonDTO create(PersonDTO dto) {
        if (dto.getDocument() != null && personRepository.existsByDocument(dto.getDocument())) {
            throw new PersonAlreadyExistsException("Person with this document already exists");
        }

        PersonEntity person = personConverter.dtoToEntity(dto);

        GroupEntity group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        person.setGroup(group);

        if (dto.getSubGroupId() != null) {
            SubGroupEntity subGroup = subGroupRepository.findById(dto.getSubGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("SubGroup not found"));
            if (!subGroup.getGroup().getId().equals(group.getId())) {
                throw new InvalidPersonDataException("SubGroup does not belong to the specified Group");
            }
            person.setSubGroup(subGroup);
        }

        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            if (dto.getCity() == null || dto.getCountry() == null || dto.getPostalCode() == null) {
                throw new InvalidPersonDataException("City, country and postal code are required for geocoding");
            }

            String address = String.format("%s, %s, %s, %s, %s",
                    dto.getAddress(), dto.getCity(), dto.getProvince(), dto.getPostalCode(), dto.getCountry());

            GeocodingResponseDTO response = geocodingClient.getCoordinates(
                    address, geoProperties.getKey());

            if (response == null || !"OK".equalsIgnoreCase(response.getStatus()) ||
                    response.getResults() == null || response.getResults().isEmpty() ||
                    response.getResults().get(0).getGeometry() == null ||
                    response.getResults().get(0).getGeometry().getLocation() == null) {
                throw new GeocodingApiException("Invalid geocoding response");
            }

            GeocodingResponseDTO.Location location = response.getResults().get(0).getGeometry().getLocation();

            GeoCoordinatesEntity coordinates = new GeoCoordinatesEntity();
            coordinates.setLatitude(location.getLat());
            coordinates.setLongitude(location.getLng());
            person.setGeoCoordinates(coordinates);
        }

        PersonEntity saved = personRepository.save(person);
        return personConverter.entityToDTO(saved);
    }

    public PersonDTO update(Long id, PersonDTO dto) {
        PersonEntity person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found"));

        if (dto.getDocument() != null && !dto.getDocument().equals(person.getDocument())) {
            if (personRepository.existsByDocument(dto.getDocument())) {
                throw new PersonAlreadyExistsException("Person with this document already exists");
            }
        }

        person.setFullName(dto.getName());
        person.setDocument(dto.getDocument());
        person.setEmail(dto.getEmail());
        person.setPhone(dto.getPhone());
        person.setRole(dto.getRole());
        person.setAge(dto.getAge());
        person.setAddress(dto.getAddress());
        person.setCity(dto.getCity());
        person.setProvince(dto.getProvince());
        person.setCountry(dto.getCountry());
        person.setPostalCode(dto.getPostalCode());
        person.setCompanyId(dto.getCompanyId());

        GroupEntity group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        person.setGroup(group);

        if (dto.getSubGroupId() != null) {
            SubGroupEntity subGroup = subGroupRepository.findById(dto.getSubGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("SubGroup not found"));
            if (!subGroup.getGroup().getId().equals(group.getId())) {
                throw new InvalidPersonDataException("SubGroup does not belong to the specified Group");
            }
            person.setSubGroup(subGroup);
        } else {
            person.setSubGroup(null);
        }

        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            String address = String.format("%s, %s, %s, %s, %s",
                    dto.getAddress(), dto.getCity(), dto.getProvince(), dto.getPostalCode(), dto.getCountry());

            GeocodingResponseDTO response = geocodingClient.getCoordinates(address, geoProperties.getKey());

            if ("OK".equals(response.getStatus())) {
                double lat = response.getResults().get(0).getGeometry().getLocation().getLat();
                double lng = response.getResults().get(0).getGeometry().getLocation().getLng();
                GeoCoordinatesEntity coordinates = person.getGeoCoordinates();
                if (coordinates == null) {
                    coordinates = new GeoCoordinatesEntity();
                }
                coordinates.setLatitude(lat);
                coordinates.setLongitude(lng);
                person.setGeoCoordinates(coordinates);
            } else {
                throw new GeocodingApiException("Failed to retrieve coordinates for the provided address");
            }
        }

        PersonEntity updated = personRepository.save(person);
        return personConverter.entityToDTO(updated);
    }

    public Optional<PersonDTO> findById(Long id) {
        return personRepository.findById(id).map(personConverter::entityToDTO);
    }

    public List<PersonDTO> findAll() {
        return personRepository.findAll()
                .stream()
                .map(personConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        PersonEntity person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found"));
        person.setDeleted(true);
        personRepository.save(person);
    }
}

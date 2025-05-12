package com.bufalari.people.service;

import com.bufalari.people.config.GeoProperties;
import com.bufalari.people.convert.PersonConverter;
import com.bufalari.people.dto.GeocodingResponseDTO;
import com.bufalari.people.dto.PersonDTO;
import com.bufalari.people.entity.GeoCoordinatesEntity;
import com.bufalari.people.entity.GroupEntity;
import com.bufalari.people.entity.PersonEntity;
import com.bufalari.people.entity.SubGroupEntity;
import com.bufalari.people.exception.GeocodingApiException;
import com.bufalari.people.exception.InvalidPersonDataException;
import com.bufalari.people.exception.PersonAlreadyExistsException;
import com.bufalari.people.exception.ResourceNotFoundException;
import com.bufalari.people.repository.GeocodingRepository;
import com.bufalari.people.repository.GroupRepository;
import com.bufalari.people.repository.PersonRepository;
import com.bufalari.people.repository.SubGroupRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID; // <<<--- IMPORT UUID
import java.util.stream.Collectors;

/**
 * Service layer for managing Person entities (using UUID).
 * Camada de serviço para gerenciar entidades Person (usando UUID).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final PersonConverter personConverter;
    private final GroupRepository groupRepository;
    private final SubGroupRepository subGroupRepository;
    private final GeocodingRepository geocodingClient;
    private final GeoProperties geoProperties;

    /**
     * Creates a new person, including geocoding if address is provided.
     * @param personDTO DTO containing person data (ID should be null).
     * @return The created PersonDTO with generated UUID.
     * @throws PersonAlreadyExistsException if document already exists.
     * @throws ResourceNotFoundException if group or subgroup is not found.
     * @throws InvalidPersonDataException if subgroup doesn't belong to the group or address parts are missing for geocoding.
     * @throws GeocodingApiException if geocoding fails.
     */
    public PersonDTO createPerson(PersonDTO personDTO) {
        log.info("Attempting to create person with document: {}", personDTO.getDocument());
        if (personDTO.getId() != null) {
            log.warn("ID provided for person creation will be ignored.");
            personDTO.setId(null);
        }

        // Validate unique document
        if (personRepository.existsByDocument(personDTO.getDocument())) {
            log.warn("Person creation failed: Document {} already exists.", personDTO.getDocument());
            throw new PersonAlreadyExistsException("Person with document " + personDTO.getDocument() + " already exists.");
        }

        PersonEntity personEntity = personConverter.dtoToEntity(personDTO);

        // Fetch and validate Group (using UUID)
        GroupEntity group = groupRepository.findById(personDTO.getGroupId()) // <<<--- Find by UUID
                .orElseThrow(() -> {
                    log.warn("Person creation failed: Group not found with ID {}", personDTO.getGroupId());
                    return new ResourceNotFoundException("Group not found with ID: " + personDTO.getGroupId());
                });
        personEntity.setGroup(group);
        log.debug("Assigned group '{}' (ID: {}) to new person.", group.getName(), group.getId());

        // Fetch and validate SubGroup (if provided, using UUID)
        if (personDTO.getSubGroupId() != null) {
            SubGroupEntity subGroup = subGroupRepository.findById(personDTO.getSubGroupId()) // <<<--- Find by UUID
                    .orElseThrow(() -> {
                        log.warn("Person creation failed: SubGroup not found with ID {}", personDTO.getSubGroupId());
                        return new ResourceNotFoundException("SubGroup not found with ID: " + personDTO.getSubGroupId());
                    });
            // Check if SubGroup belongs to the chosen Group
            if (!Objects.equals(subGroup.getGroup().getId(), group.getId())) {
                log.warn("Person creation failed: SubGroup ID {} does not belong to Group ID {}", personDTO.getSubGroupId(), group.getId());
                throw new InvalidPersonDataException("SubGroup ID " + personDTO.getSubGroupId() + " does not belong to Group ID " + group.getId());
            }
            personEntity.setSubGroup(subGroup);
            log.debug("Assigned subgroup '{}' (ID: {}) to new person.", subGroup.getName(), subGroup.getId());
        } else {
            personEntity.setSubGroup(null);
        }

        // Geocode Address
        handleGeocoding(personDTO, personEntity);

        // Save the entity
        PersonEntity savedEntity = personRepository.save(personEntity);
        log.info("Successfully created person '{}' with ID: {}", savedEntity.getFullName(), savedEntity.getId());

        return personConverter.entityToDTO(savedEntity);
    }

    /**
     * Updates an existing person.
     * @param id The UUID of the person to update.
     * @param personDTO DTO containing updated data.
     * @return The updated PersonDTO.
     * @throws ResourceNotFoundException if person, group, or subgroup is not found.
     * @throws PersonAlreadyExistsException if changing document to an existing one.
     * @throws InvalidPersonDataException if subgroup doesn't belong to group or address parts missing.
     * @throws GeocodingApiException if geocoding fails.
     */
    public PersonDTO updatePerson(UUID id, PersonDTO personDTO) { // <<<--- UUID
        log.info("Attempting to update person with ID: {}", id);

        // Find existing person or throw not found
        PersonEntity existingPerson = personRepository.findById(id) // <<<--- Find by UUID
                .orElseThrow(() -> {
                    log.warn("Person update failed: Person not found with ID {}", id);
                    return new ResourceNotFoundException("Person not found with ID: " + id);
                });

        // Validate unique document if it's being changed
        if (!Objects.equals(existingPerson.getDocument(), personDTO.getDocument())) {
            log.debug("Person document change detected for ID {}: '{}' -> '{}'", id, existingPerson.getDocument(), personDTO.getDocument());
            if (personRepository.existsByDocument(personDTO.getDocument())) {
                log.warn("Person update failed: New document {} already exists for another person.", personDTO.getDocument());
                throw new PersonAlreadyExistsException("Person with document " + personDTO.getDocument() + " already exists.");
            }
            existingPerson.setDocument(personDTO.getDocument());
        }

        // Update basic fields
        existingPerson.setFullName(personDTO.getName());
        existingPerson.setEmail(personDTO.getEmail());
        existingPerson.setPhone(personDTO.getPhone());
        existingPerson.setRole(personDTO.getRole());
        existingPerson.setAge(personDTO.getAge());
        existingPerson.setCompanyId(personDTO.getCompanyId()); // <<<--- Update UUID companyId

        // Update address fields and check if changed
        boolean addressChanged = updateAddressFields(existingPerson, personDTO);

        // Fetch and validate Group (if changed)
        if (!Objects.equals(existingPerson.getGroup().getId(), personDTO.getGroupId())) {
            log.debug("Person group change detected for ID {}", id);
            GroupEntity group = groupRepository.findById(personDTO.getGroupId()) // <<<--- Find by UUID
                    .orElseThrow(() -> {
                        log.warn("Person update failed: New group not found with ID {}", personDTO.getGroupId());
                        return new ResourceNotFoundException("Group not found with ID: " + personDTO.getGroupId());
                    });
            existingPerson.setGroup(group);
            log.debug("Updated group for person {} to '{}' (ID: {})", id, group.getName(), group.getId());
            // If group changes, subgroup *might* become invalid, needs re-check/reset
            if (existingPerson.getSubGroup() != null && !Objects.equals(existingPerson.getSubGroup().getGroup().getId(), group.getId())) {
                log.warn("Subgroup ID {} is no longer valid as group changed to {}. Removing subgroup assignment.", existingPerson.getSubGroup().getId(), group.getId());
                existingPerson.setSubGroup(null); // Or handle based on DTO's subGroupId
            }
        }

        // Fetch and validate SubGroup (if provided or changed)
        UUID currentSubGroupId = existingPerson.getSubGroup() != null ? existingPerson.getSubGroup().getId() : null;
        if (!Objects.equals(currentSubGroupId, personDTO.getSubGroupId())) {
            log.debug("Person subgroup change detected for ID {}", id);
            if (personDTO.getSubGroupId() != null) {
                SubGroupEntity subGroup = subGroupRepository.findById(personDTO.getSubGroupId()) // <<<--- Find by UUID
                        .orElseThrow(() -> {
                            log.warn("Person update failed: New subgroup not found with ID {}", personDTO.getSubGroupId());
                            return new ResourceNotFoundException("SubGroup not found with ID: " + personDTO.getSubGroupId());
                        });
                // Check if SubGroup belongs to the (potentially updated) Group
                if (!Objects.equals(subGroup.getGroup().getId(), existingPerson.getGroup().getId())) {
                    log.warn("Person update failed: New SubGroup ID {} does not belong to person's current Group ID {}", personDTO.getSubGroupId(), existingPerson.getGroup().getId());
                    throw new InvalidPersonDataException("SubGroup ID " + personDTO.getSubGroupId() + " does not belong to Group ID " + existingPerson.getGroup().getId());
                }
                existingPerson.setSubGroup(subGroup);
                log.debug("Updated subgroup for person {} to '{}' (ID: {})", id, subGroup.getName(), subGroup.getId());
            } else {
                existingPerson.setSubGroup(null); // Set subgroup to null if ID is null in DTO
                log.debug("Removed subgroup assignment for person {}", id);
            }
        }

        // Re-geocode if address fields have changed
        if (addressChanged) {
            log.debug("Address changed for person ID {}, triggering re-geocoding.", id);
            handleGeocoding(personDTO, existingPerson);
        }

        // Save the updated entity
        PersonEntity updatedEntity = personRepository.save(existingPerson);
        log.info("Successfully updated person with ID: {}", id);

        return personConverter.entityToDTO(updatedEntity);
    }


    /**
     * Retrieves a person by UUID. Respects soft delete (@Where).
     * @param id The UUID of the person.
     * @return Optional containing the PersonDTO if found and not deleted.
     */
    @Transactional(readOnly = true)
    public Optional<PersonDTO> getPersonById(UUID id) { // <<<--- UUID
        log.debug("Fetching person by ID: {}", id);
        return personRepository.findById(id).map(personConverter::entityToDTO); // <<<--- findById com UUID
    }


    /**
     * Retrieves all non-deleted persons. Respects soft delete (@Where).
     * @return List of PersonDTOs.
     */
    @Transactional(readOnly = true)
    public List<PersonDTO> getAllPersons() {
        log.debug("Fetching all non-deleted persons.");
        return personRepository.findAll() // findAll respeita @Where
                .stream()
                .map(personConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Logically deletes a person by setting the 'deleted' flag to true (uses @SQLDelete).
     * @param id The UUID of the person to delete.
     * @throws ResourceNotFoundException if the person is not found (even if deleted).
     */
    public void deletePerson(UUID id) { // <<<--- UUID
        log.info("Attempting to logically delete person with ID: {}", id);
        // Check existence first (findById respects @Where, so it won't find deleted)
        // Use existsById which also respects @Where
        if (!personRepository.existsById(id)) { // <<<--- existsById com UUID
            log.warn("Logical delete failed: Person not found with ID {}", id);
            // Note: If you need to differentiate between "not found" and "already deleted",
            // you might need a query that ignores the @Where clause or checks the deleted flag.
            throw new ResourceNotFoundException("Person not found with ID: " + id);
        }
        personRepository.deleteById(id); // <<<--- deleteById com UUID (executa @SQLDelete)
        log.info("Successfully marked person with ID {} as deleted.", id);
    }

    // --- Helper Methods ---

    /**
     * Handles the geocoding process.
     */
    private void handleGeocoding(PersonDTO dto, PersonEntity personEntity) {
        String fullAddress = buildFullAddress(dto);
        if (fullAddress == null) {
            log.debug("Geocoding skipped for person {}: Incomplete address details.", personEntity.getId() != null ? personEntity.getId() : "(new)");
            personEntity.setGeoCoordinates(null); // Clear coordinates if address is incomplete
            return;
        }

        log.debug("Attempting to geocode address for person {}: {}", personEntity.getId() != null ? personEntity.getId() : "(new)", fullAddress);

        try {
            GeocodingResponseDTO response = geocodingClient.getCoordinates(fullAddress, geoProperties.getKey());
            processGeocodingResponse(response, fullAddress, personEntity);
        } catch (FeignException e) {
            log.error("Feign error calling Geocoding API for address '{}': Status={}, Body='{}'", fullAddress, e.status(), e.contentUTF8(), e);
            // Don't set coordinates, maybe rethrow or handle gracefully
            personEntity.setGeoCoordinates(null);
            throw new GeocodingApiException("Error communicating with Geocoding service: " + e.status(), e);
        } catch (Exception e) {
            log.error("Unexpected error during geocoding for address '{}'", fullAddress, e);
            personEntity.setGeoCoordinates(null);
            throw new GeocodingApiException("Unexpected error during geocoding: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the full address string for geocoding, returns null if essential parts are missing.
     */
    private String buildFullAddress(PersonDTO dto) {
        if (StringUtils.hasText(dto.getAddress()) &&
                StringUtils.hasText(dto.getCity()) &&
                StringUtils.hasText(dto.getCountry()) &&
                StringUtils.hasText(dto.getProvince()) &&
                StringUtils.hasText(dto.getPostalCode())) {
            return String.format("%s, %s, %s, %s, %s",
                    dto.getAddress(), dto.getCity(), dto.getProvince(), dto.getPostalCode(), dto.getCountry()
            ).trim();
        }
        return null;
    }

    /**
     * Processes the response from the geocoding API and updates the entity.
     */
    private void processGeocodingResponse(GeocodingResponseDTO response, String fullAddress, PersonEntity personEntity) {
        if (response != null && "OK".equalsIgnoreCase(response.getStatus()) &&
                response.getResults() != null && !response.getResults().isEmpty())
        {
            // Get location from the first result
            GeocodingResponseDTO.Location location = response.getResults().get(0).getGeometry().getLocation();
            if (location != null) {
                GeoCoordinatesEntity coordinates = personEntity.getGeoCoordinates();
                if (coordinates == null) {
                    coordinates = new GeoCoordinatesEntity();
                }
                coordinates.setLatitude(location.getLat());
                coordinates.setLongitude(location.getLng());
                personEntity.setGeoCoordinates(coordinates);
                log.info("Geocoding successful for address: '{}'. Coordinates: Lat={}, Lng={}", fullAddress, location.getLat(), location.getLng());
                return; // Success
            }
        }
        // Handle failure cases
        String status = (response != null) ? response.getStatus() : "NULL_RESPONSE";
        String errorMsg = (response != null && response.getError_message() != null) ? response.getError_message() : "No specific error message.";
        log.warn("Geocoding failed for address '{}'. API Status: {}. Error: {}", fullAddress, status, errorMsg);
        personEntity.setGeoCoordinates(null); // Clear coordinates on failure
        // Option: Throw GeocodingApiException based on status if needed
        // if ("REQUEST_DENIED".equals(status) || "INVALID_REQUEST".equals(status)) {
        //    throw new GeocodingApiException("Geocoding failed for address '" + fullAddress + "'. Status: " + status);
        // }
    }

    /**
     * Updates address fields on the entity from the DTO, returns true if changed.
     */
    private boolean updateAddressFields(PersonEntity entity, PersonDTO dto) {
        boolean changed = false;
        if (!Objects.equals(entity.getAddress(), dto.getAddress())) { entity.setAddress(dto.getAddress()); changed = true; }
        if (!Objects.equals(entity.getCity(), dto.getCity())) { entity.setCity(dto.getCity()); changed = true; }
        if (!Objects.equals(entity.getProvince(), dto.getProvince())) { entity.setProvince(dto.getProvince()); changed = true; }
        if (!Objects.equals(entity.getCountry(), dto.getCountry())) { entity.setCountry(dto.getCountry()); changed = true; }
        if (!Objects.equals(entity.getPostalCode(), dto.getPostalCode())) { entity.setPostalCode(dto.getPostalCode()); changed = true; }
        if (changed) { log.debug("Address fields updated for person ID {}", entity.getId()); }
        return changed;
    }
}
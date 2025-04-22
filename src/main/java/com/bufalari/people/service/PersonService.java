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
import com.bufalari.people.repository.GeocodingRepository; // Use the correct repository
import com.bufalari.people.repository.GroupRepository;
import com.bufalari.people.repository.PersonRepository;
import com.bufalari.people.repository.SubGroupRepository;
import feign.FeignException; // Catch Feign specific exceptions
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // For checking blank strings

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for managing Person entities.
 */
@Service
@RequiredArgsConstructor // Use Lombok for constructor injection
@Transactional // Apply transactionality to all public methods by default
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final PersonConverter personConverter;
    private final GroupRepository groupRepository;
    private final SubGroupRepository subGroupRepository;
    private final GeocodingRepository geocodingClient; // Use the correct interface
    private final GeoProperties geoProperties;

    /**
     * Creates a new person, including geocoding if address is provided.
     * @param personDTO DTO containing person data.
     * @return The created PersonDTO.
     * @throws PersonAlreadyExistsException if document already exists.
     * @throws ResourceNotFoundException if group or subgroup is not found.
     * @throws InvalidPersonDataException if subgroup doesn't belong to the group or address parts are missing for geocoding.
     * @throws GeocodingApiException if geocoding fails.
     */
    public PersonDTO createPerson(PersonDTO personDTO) {
        log.debug("Attempting to create person with document: {}", personDTO.getDocument());

        // Validate unique document
        if (personRepository.existsByDocument(personDTO.getDocument())) {
            log.warn("Person creation failed: Document {} already exists.", personDTO.getDocument());
            throw new PersonAlreadyExistsException("Person with document " + personDTO.getDocument() + " already exists.");
        }

        PersonEntity personEntity = personConverter.dtoToEntity(personDTO);

        // Fetch and validate Group
        GroupEntity group = groupRepository.findById(personDTO.getGroupId())
                .orElseThrow(() -> {
                    log.warn("Person creation failed: Group not found with ID {}", personDTO.getGroupId());
                    return new ResourceNotFoundException("Group not found with ID: " + personDTO.getGroupId());
                });
        personEntity.setGroup(group);
        log.debug("Assigned group '{}' to new person.", group.getName());

        // Fetch and validate SubGroup (if provided)
        if (personDTO.getSubGroupId() != null) {
            SubGroupEntity subGroup = subGroupRepository.findById(personDTO.getSubGroupId())
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
            log.debug("Assigned subgroup '{}' to new person.", subGroup.getName());
        } else {
             personEntity.setSubGroup(null); // Ensure subgroup is null if ID is not provided
        }


        // Geocode Address (if provided)
        handleGeocoding(personDTO, personEntity);

        // Save the entity
        PersonEntity savedEntity = personRepository.save(personEntity);
        log.info("Successfully created person with ID: {}", savedEntity.getId());

        // Convert back to DTO for response
        return personConverter.entityToDTO(savedEntity);
    }

    /**
     * Updates an existing person.
     * @param id The ID of the person to update.
     * @param personDTO DTO containing updated data.
     * @return The updated PersonDTO.
     * @throws ResourceNotFoundException if person, group, or subgroup is not found.
     * @throws PersonAlreadyExistsException if changing document to an existing one.
     * @throws InvalidPersonDataException if subgroup doesn't belong to group or address parts missing.
     * @throws GeocodingApiException if geocoding fails.
     */
    public PersonDTO updatePerson(Long id, PersonDTO personDTO) {
        log.debug("Attempting to update person with ID: {}", id);

        // Find existing person or throw not found
        PersonEntity existingPerson = personRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Person update failed: Person not found with ID {}", id);
                    return new ResourceNotFoundException("Person not found with ID: " + id);
                });

        // Validate unique document if it's being changed
        if (!Objects.equals(existingPerson.getDocument(), personDTO.getDocument())) {
            if (personRepository.existsByDocument(personDTO.getDocument())) {
                log.warn("Person update failed: Document {} already exists for another person.", personDTO.getDocument());
                throw new PersonAlreadyExistsException("Person with document " + personDTO.getDocument() + " already exists.");
            }
            existingPerson.setDocument(personDTO.getDocument()); // Update document
        }

        // Update basic fields from DTO
        existingPerson.setFullName(personDTO.getName()); // Map DTO 'name' to entity 'fullName'
        existingPerson.setEmail(personDTO.getEmail());
        existingPerson.setPhone(personDTO.getPhone());
        existingPerson.setRole(personDTO.getRole());
        existingPerson.setAge(personDTO.getAge());
        existingPerson.setCompanyId(personDTO.getCompanyId()); // Update company ID

        // Update address fields
        boolean addressChanged = updateAddressFields(existingPerson, personDTO);


        // Fetch and validate Group
        if (!Objects.equals(existingPerson.getGroup().getId(), personDTO.getGroupId())) {
            GroupEntity group = groupRepository.findById(personDTO.getGroupId())
                    .orElseThrow(() -> {
                         log.warn("Person update failed: Group not found with ID {}", personDTO.getGroupId());
                         return new ResourceNotFoundException("Group not found with ID: " + personDTO.getGroupId());
                     });
            existingPerson.setGroup(group);
            log.debug("Updated group for person {} to '{}'", id, group.getName());
        }

        // Fetch and validate SubGroup (if provided or changed)
        Long currentSubGroupId = existingPerson.getSubGroup() != null ? existingPerson.getSubGroup().getId() : null;
        if (!Objects.equals(currentSubGroupId, personDTO.getSubGroupId())) {
             if (personDTO.getSubGroupId() != null) {
                  SubGroupEntity subGroup = subGroupRepository.findById(personDTO.getSubGroupId())
                          .orElseThrow(() -> {
                              log.warn("Person update failed: SubGroup not found with ID {}", personDTO.getSubGroupId());
                              return new ResourceNotFoundException("SubGroup not found with ID: " + personDTO.getSubGroupId());
                          });
                 // Check if SubGroup belongs to the (potentially updated) Group
                 if (!Objects.equals(subGroup.getGroup().getId(), existingPerson.getGroup().getId())) {
                     log.warn("Person update failed: SubGroup ID {} does not belong to Group ID {}", personDTO.getSubGroupId(), existingPerson.getGroup().getId());
                     throw new InvalidPersonDataException("SubGroup ID " + personDTO.getSubGroupId() + " does not belong to Group ID " + existingPerson.getGroup().getId());
                 }
                 existingPerson.setSubGroup(subGroup);
                 log.debug("Updated subgroup for person {} to '{}'", id, subGroup.getName());
             } else {
                 existingPerson.setSubGroup(null); // Set subgroup to null if ID is null in DTO
                 log.debug("Removed subgroup assignment for person {}", id);
             }
        }

        // Re-geocode if address fields have changed
        if (addressChanged) {
             handleGeocoding(personDTO, existingPerson); // Use updated DTO data for geocoding
        }


        // Save the updated entity
        PersonEntity updatedEntity = personRepository.save(existingPerson);
        log.info("Successfully updated person with ID: {}", id);

        // Convert back to DTO
        return personConverter.entityToDTO(updatedEntity);
    }


     /**
      * Retrieves a person by ID.
      * @param id The ID of the person.
      * @return Optional containing the PersonDTO if found.
      */
     @Transactional(readOnly = true) // Mark as read-only transaction
     public Optional<PersonDTO> getPersonById(Long id) {
         log.debug("Fetching person by ID: {}", id);
         // findById respects the @Where clause (deleted=false)
         return personRepository.findById(id).map(personConverter::entityToDTO);
     }


     /**
      * Retrieves all non-deleted persons.
      * @return List of PersonDTOs.
      */
     @Transactional(readOnly = true)
     public List<PersonDTO> getAllPersons() {
         log.debug("Fetching all non-deleted persons.");
         // findAll respects the @Where clause
         return personRepository.findAll()
                 .stream()
                 .map(personConverter::entityToDTO)
                 .collect(Collectors.toList());
     }

    /**
     * Logically deletes a person by setting the 'deleted' flag to true.
     * @param id The ID of the person to delete.
     * @throws ResourceNotFoundException if the person is not found.
     */
    public void deletePerson(Long id) {
        log.debug("Attempting to logically delete person with ID: {}", id);
        // Use standard deleteById which is overridden by @SQLDelete
        if (!personRepository.existsById(id)) {
             log.warn("Logical delete failed: Person not found with ID {}", id);
             throw new ResourceNotFoundException("Person not found with ID: " + id);
        }
        personRepository.deleteById(id); // This will execute the UPDATE defined in @SQLDelete
        log.info("Successfully marked person with ID {} as deleted.", id);
    }

    // --- Helper Methods ---

    /**
     * Handles the geocoding process for a person entity based on DTO data.
     * Updates the entity's geoCoordinates.
     */
    private void handleGeocoding(PersonDTO dto, PersonEntity personEntity) {
         // Check if address components are present for geocoding attempt
        if (StringUtils.hasText(dto.getAddress()) &&
            StringUtils.hasText(dto.getCity()) &&
            StringUtils.hasText(dto.getCountry()) &&
            StringUtils.hasText(dto.getProvince()) && // Assuming province is needed too
            StringUtils.hasText(dto.getPostalCode()))
        {
            String fullAddress = String.format("%s, %s, %s, %s, %s",
                    dto.getAddress(),
                    dto.getCity(),
                    dto.getProvince(),
                    dto.getPostalCode(),
                    dto.getCountry()
            ).trim(); // Trim whitespace

            log.debug("Attempting to geocode address: {}", fullAddress);

            try {
                GeocodingResponseDTO response = geocodingClient.getCoordinates(fullAddress, geoProperties.getKey());

                if (response != null && "OK".equalsIgnoreCase(response.getStatus()) &&
                    response.getResults() != null && !response.getResults().isEmpty() &&
                    response.getResults().get(0).getGeometry() != null &&
                    response.getResults().get(0).getGeometry().getLocation() != null)
                {
                    GeocodingResponseDTO.Location location = response.getResults().get(0).getGeometry().getLocation();
                    GeoCoordinatesEntity coordinates = personEntity.getGeoCoordinates();
                    if (coordinates == null) {
                        coordinates = new GeoCoordinatesEntity();
                    }
                    coordinates.setLatitude(location.getLat());
                    coordinates.setLongitude(location.getLng());
                    personEntity.setGeoCoordinates(coordinates);
                    log.info("Geocoding successful for address: {}. Coordinates: Lat={}, Lng={}", fullAddress, location.getLat(), location.getLng());

                } else {
                    String status = (response != null) ? response.getStatus() : "NULL_RESPONSE";
                    log.warn("Geocoding failed for address '{}'. API Status: {}", fullAddress, status);
                    // Decide if this should be a hard error or just a warning
                    // Option 1: Throw exception
                    // throw new GeocodingApiException("Geocoding failed for the provided address. Status: " + status);
                    // Option 2: Log warning and clear coordinates
                     personEntity.setGeoCoordinates(null); // Clear coordinates if geocoding fails
                }
            } catch (FeignException e) {
                 log.error("Error calling Geocoding API for address '{}': Status={}, Body={}", fullAddress, e.status(), e.contentUTF8(), e);
                 throw new GeocodingApiException("Error communicating with Geocoding service: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("An unexpected error occurred during geocoding for address '{}'", fullAddress, e);
                throw new GeocodingApiException("Unexpected error during geocoding: " + e.getMessage(), e);
            }

        } else if (StringUtils.hasText(dto.getAddress()) || StringUtils.hasText(dto.getCity()) ||
                   StringUtils.hasText(dto.getCountry()) || StringUtils.hasText(dto.getPostalCode())) {
            // If some address parts are provided but not all required for geocoding
             log.warn("Geocoding skipped: Incomplete address details provided for person {}. Address, City, Province, Postal Code, and Country are required.", personEntity.getId() != null ? personEntity.getId() : "(new)");
             personEntity.setGeoCoordinates(null); // Clear coordinates if address is incomplete
             // Option: Throw InvalidPersonDataException if partial address requires full geocoding components
             // throw new InvalidPersonDataException("Address, City, Province, Postal Code, and Country are required for geocoding if any address part is provided.");
        } else {
             // No address provided, clear coordinates
             log.debug("No address provided, clearing geocoordinates for person {}", personEntity.getId() != null ? personEntity.getId() : "(new)");
             personEntity.setGeoCoordinates(null);
        }
    }

     /**
      * Updates address fields on the entity from the DTO if they have changed.
      * @return true if any address field was changed, false otherwise.
      */
     private boolean updateAddressFields(PersonEntity entity, PersonDTO dto) {
         boolean changed = false;
         if (!Objects.equals(entity.getAddress(), dto.getAddress())) {
             entity.setAddress(dto.getAddress());
             changed = true;
         }
         if (!Objects.equals(entity.getCity(), dto.getCity())) {
             entity.setCity(dto.getCity());
             changed = true;
         }
         if (!Objects.equals(entity.getProvince(), dto.getProvince())) {
             entity.setProvince(dto.getProvince());
             changed = true;
         }
         if (!Objects.equals(entity.getCountry(), dto.getCountry())) {
             entity.setCountry(dto.getCountry());
             changed = true;
         }
         if (!Objects.equals(entity.getPostalCode(), dto.getPostalCode())) {
             entity.setPostalCode(dto.getPostalCode());
             changed = true;
         }
         if (changed) {
             log.debug("Address fields updated for person ID {}", entity.getId());
         }
         return changed;
     }
}
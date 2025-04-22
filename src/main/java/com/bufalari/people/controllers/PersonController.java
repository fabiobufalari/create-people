package com.bufalari.people.controllers;

import com.bufalari.people.client.AuthServiceClient;
import com.bufalari.people.dto.PersonDTO;
import com.bufalari.people.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus; // For 201 Created
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Example for method-level security
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // For Location header

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing people.
 */
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor // Use Lombok for constructor injection
@Tag(name = "Person Management", description = "Endpoints for creating, retrieving, updating, and deleting people") // Swagger Tag
@SecurityRequirement(name = "bearerAuth") // Apply security requirement to all methods in this controller
public class PersonController {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;
    private final AuthServiceClient authServiceClient; // Keep Feign client for user details if needed elsewhere, or remove if only used for logging

    /**
     * Creates a new person.
     *
     * @param personDTO The data of the person to be created.
     * @param authorizationHeader The JWT token for authentication.
     * @return ResponseEntity with the created person and Location header.
     */
    @Operation(summary = "Creates a new person", description = "Requires authentication. Validates input data and geocodes address if provided.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Person created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data provided (validation error, duplicate document, invalid group/subgroup, geocoding issue)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
        @ApiResponse(responseCode = "404", description = "Group or SubGroup not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // Example: Add method-level security if needed
    public ResponseEntity<PersonDTO> createPerson(
            @Valid @RequestBody PersonDTO personDTO,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) { // Hide auth header from Swagger UI input
        logger.info("Request received to create person: {}", personDTO.getName());

        // Optional: Log authenticated user (if needed beyond just logging)
        try {
             Object userDetailsObject = authServiceClient.getUserDetails(authorizationHeader);
             if (userDetailsObject instanceof Map) {
                 @SuppressWarnings("unchecked") // Suppress warning as we check instance type
                 Map<String, Object> userDetails = (Map<String, Object>) userDetailsObject;
                 String username = (String) userDetails.get("username");
                 logger.debug("Authenticated user performing creation: {}", username);
             } else {
                 logger.warn("Received unexpected user details format from auth service.");
             }
        } catch (Exception e) {
            logger.error("Failed to retrieve user details from auth service", e);
            // Decide if this should prevent person creation or just log the error
        }


        PersonDTO createdPerson = personService.createPerson(personDTO);
        logger.info("Person created successfully with ID: {}", createdPerson.getId());

        // Build Location URI for the newly created resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPerson.getId())
                .toUri();

        // Return 201 Created status with Location header and the created person DTO
        return ResponseEntity.created(location).body(createdPerson);
    }

    /**
     * Retrieves a person by their ID.
     *
     * @param id The ID of the person to retrieve.
     * @return ResponseEntity containing the person DTO if found.
     */
    @Operation(summary = "Gets a person by ID", description = "Requires authentication.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Person found and returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Person not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable Long id) {
        logger.info("Request received to get person by ID: {}", id);
        // findById now returns Optional, handle the not found case
        return personService.getPersonById(id)
                .map(personDTO -> {
                    logger.info("Person found with ID: {}", id);
                    return ResponseEntity.ok(personDTO);
                })
                .orElseGet(() -> {
                    logger.warn("Person not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }


    /**
     * Lists all non-deleted people.
     *
     * @return ResponseEntity with a list of all people.
     */
    @Operation(summary = "Lists all people", description = "Requires authentication. Returns non-deleted people.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of people returned successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        logger.info("Request received to list all persons");
        List<PersonDTO> persons = personService.getAllPersons();
        logger.info("Returning {} persons", persons.size());
        return ResponseEntity.ok(persons);
    }

     /**
     * Updates an existing person.
     *
     * @param id The ID of the person to update.
     * @param personDTO The updated data for the person.
     * @return ResponseEntity with the updated person DTO.
     */
    @Operation(summary = "Updates an existing person", description = "Requires authentication. Validates input data and geocodes address if provided/changed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Person updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data provided (validation error, duplicate document, invalid group/subgroup, geocoding issue)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Person, Group or SubGroup not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable Long id, @Valid @RequestBody PersonDTO personDTO) {
        logger.info("Request received to update person with ID: {}", id);
        PersonDTO updatedPerson = personService.updatePerson(id, personDTO);
        logger.info("Person updated successfully with ID: {}", id);
        return ResponseEntity.ok(updatedPerson);
    }

    /**
     * Logically deletes a person by their ID.
     *
     * @param id The ID of the person to delete.
     * @return ResponseEntity with no content.
     */
    @Operation(summary = "Deletes a person by ID (Logical)", description = "Requires authentication. Performs a logical delete (marks as deleted).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Person deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Person not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        logger.info("Request received to delete person with ID: {}", id);
        personService.deletePerson(id);
        logger.info("Person logically deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
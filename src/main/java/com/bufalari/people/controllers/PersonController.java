package com.bufalari.people.controllers;

import com.bufalari.people.client.AuthServiceClient;
import com.bufalari.people.dto.PersonDTO;
import com.bufalari.people.dto.UserDetailsDTO;
import com.bufalari.people.service.PersonService;
import feign.FeignException; // <<<--- IMPORT FeignException
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/create-people/persons")
@RequiredArgsConstructor
@Tag(name = "Person Management", description = "Endpoints for creating, retrieving, updating, and deleting people")
@SecurityRequirement(name = "bearerAuth")
public class PersonController {

    private static final Logger log = LoggerFactory.getLogger(PersonController.class);

    private final PersonService personService;
    private final AuthServiceClient authServiceClient;

    @Operation(summary = "Creates a new person")
    @ApiResponses(/* ... */)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<PersonDTO> createPerson(
            @Valid @RequestBody PersonDTO personDTO,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        log.info("Request received to create person: {}", personDTO.getName());
        logAuthenticatedUser(authorizationHeader, "createPerson");
        PersonDTO createdPerson = personService.createPerson(personDTO);
        log.info("Person created successfully with ID: {}", createdPerson.getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPerson.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdPerson);
    }

    @Operation(summary = "Gets a person by ID")
    @ApiResponses(/* ... */)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PersonDTO> getPersonById(
            @Parameter(description = "UUID of the person") @PathVariable UUID id) {
        log.info("Request received to get person by ID: {}", id);
        return personService.getPersonById(id)
                .map(personDTO -> {
                    log.info("Person found with ID: {}", id);
                    return ResponseEntity.ok(personDTO);
                })
                .orElseGet(() -> {
                    log.warn("Person not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(summary = "Lists all people")
    @ApiResponses(/* ... */)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        log.info("Request received to list all persons");
        List<PersonDTO> persons = personService.getAllPersons();
        log.info("Returning {} persons", persons.size());
        return ResponseEntity.ok(persons);
    }

    @Operation(summary = "Updates an existing person")
    @ApiResponses(/* ... */)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<PersonDTO> updatePerson(
            @Parameter(description = "UUID of the person to update") @PathVariable UUID id,
            @Valid @RequestBody PersonDTO personDTO,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        log.info("Request received to update person with ID: {}", id);
        logAuthenticatedUser(authorizationHeader, "updatePerson");
        if (personDTO.getId() != null && !personDTO.getId().equals(id)) {
            log.warn("Path ID {} does not match body ID {}. Using path ID for update.", id, personDTO.getId());
        }
        PersonDTO updatedPerson = personService.updatePerson(id, personDTO);
        log.info("Person updated successfully with ID: {}", id);
        return ResponseEntity.ok(updatedPerson);
    }

    @Operation(summary = "Deletes a person by ID (Logical)")
    @ApiResponses(/* ... */)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deletePerson(
            @Parameter(description = "UUID of the person to delete") @PathVariable UUID id,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        log.info("Request received to delete person with ID: {}", id);
        logAuthenticatedUser(authorizationHeader, "deletePerson");
        personService.deletePerson(id);
        log.info("Person logically deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Logs the username of the authenticated user performing an action.
     * Logs warnings or errors if user details cannot be retrieved.
     * @param authorizationHeader The full Authorization header (Bearer token).
     * @param action A description of the action being performed (for logging).
     */
    private void logAuthenticatedUser(String authorizationHeader, String action) {
        // Verifica se o header é nulo ou não começa com Bearer
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Cannot log authenticated user for action '{}': Invalid or missing Authorization header.", action);
            return;
        }

        try {
            // Chama o método correto no cliente Feign
            UserDetailsDTO userDetails = authServiceClient.getUserDetailsFromToken(authorizationHeader);

            // Verifica se o DTO e o username não são nulos
            if (userDetails != null && userDetails.getUsername() != null) {
                log.debug("User '{}' performing action: {}", userDetails.getUsername(), action);
            } else {
                // Se userDetails for nulo ou getUsername for nulo (inesperado se o endpoint /me funcionar)
                log.warn("Could not determine authenticated user details for action '{}'. UserDetailsDTO or username was null.", action);
            }
        } catch (FeignException e) {
            // Trata erros específicos do Feign de forma mais granular
            log.error("FeignException while retrieving user details from auth service during action '{}'. Status: {}, Message: {}",
                    action, e.status(), e.getMessage());
            // Você pode logar o e.contentUTF8() se precisar do corpo da resposta do erro, mas cuidado com dados sensíveis
            // log.error("Response Body: {}", e.contentUTF8());
            // Não relançar a exceção aqui, pois é apenas para logging
        } catch (Exception e) {
            // Captura outras exceções inesperadas
            log.error("Unexpected exception while retrieving user details from auth service during action '{}': {}",
                    action, e.getMessage(), e); // Loga a stack trace para erros inesperados
        }
    }
}
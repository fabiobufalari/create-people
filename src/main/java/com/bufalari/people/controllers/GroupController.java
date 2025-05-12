package com.bufalari.people.controllers;

import com.bufalari.people.dto.GroupDTO;
import com.bufalari.people.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter; // Importar Parameter
import io.swagger.v3.oas.annotations.media.Content; // Importar Content
import io.swagger.v3.oas.annotations.media.Schema; // Importar Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory; // Importar LoggerFactory
import org.springframework.http.MediaType; // Importar MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importar @PreAuthorize
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "Endpoints for managing person groups")
@SecurityRequirement(name = "bearerAuth") // Aplica segurança a todos os endpoints
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);
    private final GroupService groupService;

    @Operation(summary = "Creates a new group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., name missing or duplicate)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g., name already exists)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Exemplo: Apenas ADMIN pode criar grupos
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody GroupDTO groupDTO) {
        log.info("Request received to create group: {}", groupDTO.getName());
        GroupDTO createdGroup = groupService.createGroup(groupDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdGroup.getId()) // Usa o UUID retornado
                .toUri();
        log.info("Group created successfully with ID {} at {}", createdGroup.getId(), location);
        return ResponseEntity.created(location).body(createdGroup);
    }

    @Operation(summary = "Gets a group by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GroupDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()") // Qualquer usuário autenticado pode ver
    public ResponseEntity<GroupDTO> getGroupById(
            @Parameter(description = "UUID of the group") @PathVariable UUID id) { // <<<--- UUID
        log.debug("Request received to get group by ID: {}", id);
        GroupDTO group = groupService.getGroupById(id); // Serviço agora lança exceção se não encontrar
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Lists all groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups listed successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        log.debug("Request received to list all groups");
        List<GroupDTO> groups = groupService.getAllGroups();
        log.info("Returning {} groups", groups.size());
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Updates an existing group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or Group name already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g., name already exists)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Exemplo: Apenas ADMIN pode atualizar
    public ResponseEntity<GroupDTO> updateGroup(
            @Parameter(description = "UUID of the group to update") @PathVariable UUID id, // <<<--- UUID
            @Valid @RequestBody GroupDTO groupDTO) {
        log.info("Request received to update group ID: {}", id);
        // Opcional: Verificar se ID no path e body coincidem
        if (groupDTO.getId() != null && !groupDTO.getId().equals(id)) {
            log.warn("Path ID {} does not match body ID {}. Using path ID for update.", id, groupDTO.getId());
        }
        GroupDTO updatedGroup = groupService.updateGroup(id, groupDTO);
        log.info("Group updated successfully for ID: {}", id);
        return ResponseEntity.ok(updatedGroup);
    }

    @Operation(summary = "Deletes a group by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete group with associated people or subgroups"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Exemplo: Apenas ADMIN pode deletar
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "UUID of the group to delete") @PathVariable UUID id) { // <<<--- UUID
        log.info("Request received to delete group ID: {}", id);
        groupService.deleteGroup(id); // Serviço lança exceção se não encontrar ou se houver dependências
        log.info("Group deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}
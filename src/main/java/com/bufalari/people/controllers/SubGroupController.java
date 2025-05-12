package com.bufalari.people.controllers;

import com.bufalari.people.dto.SubGroupDTO;
import com.bufalari.people.service.SubGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content; // Importar
import io.swagger.v3.oas.annotations.media.Schema; // Importar
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
@RequestMapping("/api/subgroups")
@RequiredArgsConstructor
@Tag(name = "SubGroup Management", description = "Endpoints for managing person subgroups")
@SecurityRequirement(name = "bearerAuth") // Aplica segurança a todos
public class SubGroupController {

    private static final Logger log = LoggerFactory.getLogger(SubGroupController.class);
    private final SubGroupService subGroupService;

    @Operation(summary = "Creates a new subgroup under a parent group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subgroup created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SubGroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., name or groupId missing, duplicate name in group)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Parent Group not found"),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g., name already exists in parent group)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Exemplo: Apenas ADMIN pode criar
    public ResponseEntity<SubGroupDTO> createSubGroup(@Valid @RequestBody SubGroupDTO subGroupDTO) {
        log.info("Request received to create subgroup '{}' under group ID {}", subGroupDTO.getName(), subGroupDTO.getGroupId());
        SubGroupDTO createdSubGroup = subGroupService.createSubGroup(subGroupDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSubGroup.getId()) // Usa o UUID retornado
                .toUri();
        log.info("Subgroup created successfully with ID {} at {}", createdSubGroup.getId(), location);
        return ResponseEntity.created(location).body(createdSubGroup);
    }

    @Operation(summary = "Gets a subgroup by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subgroup found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SubGroupDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Subgroup not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()") // Qualquer usuário autenticado pode ver
    public ResponseEntity<SubGroupDTO> getSubGroupById(
            @Parameter(description = "UUID of the subgroup") @PathVariable UUID id) { // <<<--- UUID
        log.debug("Request received to get subgroup by ID: {}", id);
        SubGroupDTO subGroup = subGroupService.getSubGroupById(id); // Serviço lança exceção se não encontrar
        return ResponseEntity.ok(subGroup);
    }

    @Operation(summary = "Lists all subgroups or subgroups by parent group ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subgroups listed successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Parent Group not found (if groupId provided)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SubGroupDTO>> getAllSubGroups(
            @Parameter(description = "Optional: Filter subgroups by parent group UUID")
            @RequestParam(value = "groupId", required = false) UUID groupId) { // <<<--- UUID
        List<SubGroupDTO> subGroups;
        if (groupId != null) {
            log.debug("Request received to list subgroups for group ID: {}", groupId);
            subGroups = subGroupService.getSubGroupsByGroup(groupId); // Serviço lança exceção se grupo pai não existir
        } else {
            log.debug("Request received to list all subgroups");
            subGroups = subGroupService.getAllSubGroups();
        }
        log.info("Returning {} subgroups.", subGroups.size());
        return ResponseEntity.ok(subGroups);
    }

    @Operation(summary = "Updates an existing subgroup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subgroup updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SubGroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or duplicate name in target group"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Subgroup or (new) parent Group not found"),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g., name already exists in target group)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Exemplo: Apenas ADMIN pode atualizar
    public ResponseEntity<SubGroupDTO> updateSubGroup(
            @Parameter(description = "UUID of the subgroup to update") @PathVariable UUID id, // <<<--- UUID
            @Valid @RequestBody SubGroupDTO subGroupDTO) {
        log.info("Request received to update subgroup ID: {}", id);
        // Opcional: Verificar se ID no path e body coincidem
        if (subGroupDTO.getId() != null && !subGroupDTO.getId().equals(id)) {
            log.warn("Path ID {} does not match body ID {}. Using path ID for update.", id, subGroupDTO.getId());
        }
        SubGroupDTO updatedSubGroup = subGroupService.updateSubGroup(id, subGroupDTO); // Serviço lança exceções
        log.info("Subgroup updated successfully for ID: {}", id);
        return ResponseEntity.ok(updatedSubGroup);
    }

    @Operation(summary = "Deletes a subgroup by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subgroup deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete subgroup with associated people"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Subgroup not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Exemplo: Apenas ADMIN pode deletar
    public ResponseEntity<Void> deleteSubGroup(
            @Parameter(description = "UUID of the subgroup to delete") @PathVariable UUID id) { // <<<--- UUID
        log.info("Request received to delete subgroup ID: {}", id);
        subGroupService.deleteSubGroup(id); // Serviço lança exceções
        log.info("Subgroup deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}
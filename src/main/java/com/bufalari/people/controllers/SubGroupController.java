package com.bufalari.people.controllers;

import com.bufalari.people.dto.SubGroupDTO;
import com.bufalari.people.service.SubGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/subgroups")
@RequiredArgsConstructor
@Tag(name = "SubGroup Management", description = "Endpoints for managing person subgroups")
@SecurityRequirement(name = "bearerAuth") // Apply security to all subgroup endpoints
public class SubGroupController {

    private final SubGroupService subGroupService; // Renamed service to subGroupService

    @Operation(summary = "Creates a new subgroup")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Subgroup created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data (e.g., name or groupId missing)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Parent Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<SubGroupDTO> createSubGroup(@Valid @RequestBody SubGroupDTO subGroupDTO) { // Renamed dto to subGroupDTO, create to createSubGroup
        SubGroupDTO createdSubGroup = subGroupService.createSubGroup(subGroupDTO); // Renamed create to createSubGroup
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSubGroup.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdSubGroup);
    }

    @Operation(summary = "Gets a subgroup by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subgroup found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Subgroup not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubGroupDTO> getSubGroupById(@PathVariable Long id) { // Renamed getById to getSubGroupById
        return ResponseEntity.ok(subGroupService.getSubGroupById(id)); // Renamed getById to getSubGroupById
    }

    @Operation(summary = "Lists all subgroups or subgroups by parent group ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subgroups listed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Parent Group not found (if groupId provided)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<SubGroupDTO>> getAllSubGroups(
            @Parameter(description = "Optional: Filter subgroups by parent group ID")
            @RequestParam(value = "groupId", required = false) Long groupId) { // Renamed getAll to getAllSubGroups
        List<SubGroupDTO> subGroups;
        if (groupId != null) {
            subGroups = subGroupService.getSubGroupsByGroup(groupId); // Renamed getByGroup to getSubGroupsByGroup
        } else {
            subGroups = subGroupService.getAllSubGroups(); // Renamed getAll to getAllSubGroups
        }
        return ResponseEntity.ok(subGroups);
    }

    @Operation(summary = "Updates an existing subgroup")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subgroup updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Subgroup or parent Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SubGroupDTO> updateSubGroup(@PathVariable Long id, @Valid @RequestBody SubGroupDTO subGroupDTO) { // Renamed dto to subGroupDTO, update to updateSubGroup
        return ResponseEntity.ok(subGroupService.updateSubGroup(id, subGroupDTO)); // Renamed update to updateSubGroup
    }

    @Operation(summary = "Deletes a subgroup by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Subgroup deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete subgroup with associated people"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Subgroup not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubGroup(@PathVariable Long id) { // Renamed delete to deleteSubGroup
        subGroupService.deleteSubGroup(id); // Renamed delete to deleteSubGroup
        return ResponseEntity.noContent().build();
    }
}
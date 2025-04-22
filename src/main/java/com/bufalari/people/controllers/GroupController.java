package com.bufalari.people.controllers;

import com.bufalari.people.dto.GroupDTO;
import com.bufalari.people.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "Endpoints for managing person groups")
@SecurityRequirement(name = "bearerAuth") // Apply security to all group endpoints
public class GroupController {

    private final GroupService groupService; // Renamed from service to groupService

    @Operation(summary = "Creates a new group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Group created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data (e.g., name missing) or Group name already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@Valid @RequestBody GroupDTO groupDTO) { // Renamed dto to groupDTO
        GroupDTO createdGroup = groupService.createGroup(groupDTO); // Renamed create to createGroup
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdGroup.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdGroup);
    }

    @Operation(summary = "Gets a group by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Group found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable Long id) { // Renamed getById to getGroupById
        return ResponseEntity.ok(groupService.getGroupById(id)); // Renamed getById to getGroupById
    }

    @Operation(summary = "Lists all groups")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Groups listed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() { // Renamed getAll to getAllGroups
        return ResponseEntity.ok(groupService.getAllGroups()); // Renamed getAll to getAllGroups
    }

    @Operation(summary = "Updates an existing group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Group updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data or Group name already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable Long id, @Valid @RequestBody GroupDTO groupDTO) { // Renamed dto to groupDTO, update to updateGroup
        return ResponseEntity.ok(groupService.updateGroup(id, groupDTO)); // Renamed update to updateGroup
    }

    @Operation(summary = "Deletes a group by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete group with associated people or subgroups"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) { // Renamed delete to deleteGroup
        groupService.deleteGroup(id); // Renamed delete to deleteGroup
        return ResponseEntity.noContent().build();
    }
}
package com.bufalari.people.service;

import com.bufalari.people.convert.GroupConverter;
import com.bufalari.people.dto.GroupDTO;
import com.bufalari.people.entity.GroupEntity;
import com.bufalari.people.exception.OperationNotAllowedException;
import com.bufalari.people.exception.ResourceAlreadyExistsException;
import com.bufalari.people.exception.ResourceNotFoundException;
import com.bufalari.people.repository.GroupRepository;
import com.bufalari.people.repository.PersonRepository;
import com.bufalari.people.repository.SubGroupRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID; // <<<--- IMPORT UUID
import java.util.stream.Collectors;

/**
 * Service layer for managing Group entities (using UUID).
 * Camada de serviço para gerenciar entidades Group (usando UUID).
 */
@Service
@RequiredArgsConstructor // Injeta dependências finais via construtor
@Transactional // Transacionalidade padrão
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final PersonRepository personRepository;
    private final SubGroupRepository subGroupRepository;
    private final GroupConverter groupConverter;

    /**
     * Creates a new group.
     * @param groupDTO DTO containing group data (ID should be null).
     * @return The created GroupDTO with generated UUID.
     * @throws ResourceAlreadyExistsException if group name already exists.
     */
    public GroupDTO createGroup(GroupDTO groupDTO) {
        log.info("Attempting to create group with name: {}", groupDTO.getName());
        if (groupDTO.getId() != null) {
            log.warn("ID provided for group creation will be ignored.");
            groupDTO.setId(null);
        }
        // Check for duplicate group name
        if (groupRepository.existsByName(groupDTO.getName())) {
            log.warn("Group creation failed: Name '{}' already exists.", groupDTO.getName());
            throw new ResourceAlreadyExistsException("Group with name '" + groupDTO.getName() + "' already exists.");
        }

        GroupEntity groupEntity = groupConverter.dtoToEntity(groupDTO);
        GroupEntity savedEntity = groupRepository.save(groupEntity);
        log.info("Successfully created group '{}' with ID: {}", savedEntity.getName(), savedEntity.getId());
        return groupConverter.entityToDTO(savedEntity);
    }

    /**
     * Updates an existing group.
     * @param id The UUID of the group to update.
     * @param groupDTO DTO containing updated data.
     * @return The updated GroupDTO.
     * @throws ResourceNotFoundException if group is not found.
     * @throws ResourceAlreadyExistsException if changing name to an existing one.
     */
    public GroupDTO updateGroup(UUID id, GroupDTO groupDTO) { // <<<--- UUID
        log.info("Attempting to update group with ID: {}", id);
        GroupEntity existingGroup = groupRepository.findById(id) // <<<--- findById com UUID
                .orElseThrow(() -> {
                    log.warn("Group update failed: Group not found with ID {}", id);
                    return new ResourceNotFoundException("Group not found with ID: " + id);
                });

        // Check if the name is being changed and if the new name already exists
        if (!Objects.equals(existingGroup.getName(), groupDTO.getName())) {
            log.debug("Group name change detected for ID {}: '{}' -> '{}'", id, existingGroup.getName(), groupDTO.getName());
            if (groupRepository.existsByName(groupDTO.getName())) {
                log.warn("Group update failed: New name '{}' already exists for another group.", groupDTO.getName());
                throw new ResourceAlreadyExistsException("Group with name '" + groupDTO.getName() + "' already exists.");
            }
            existingGroup.setName(groupDTO.getName()); // Update name
        }

        // Update type if changed
        if (!Objects.equals(existingGroup.getType(), groupDTO.getType())) {
            log.debug("Group type change detected for ID {}: '{}' -> '{}'", id, existingGroup.getType(), groupDTO.getType());
            existingGroup.setType(groupDTO.getType());
        }

        GroupEntity updatedEntity = groupRepository.save(existingGroup);
        log.info("Successfully updated group with ID: {}", id);
        return groupConverter.entityToDTO(updatedEntity);
    }

    /**
     * Retrieves a group by its UUID.
     * @param id The UUID of the group.
     * @return The GroupDTO.
     * @throws ResourceNotFoundException if group is not found.
     */
    @Transactional(readOnly = true)
    public GroupDTO getGroupById(UUID id) { // <<<--- UUID
        log.debug("Fetching group by ID: {}", id);
        return groupRepository.findById(id) // <<<--- findById com UUID
                .map(groupConverter::entityToDTO)
                .orElseThrow(() -> {
                    log.warn("Group retrieval failed: Group not found with ID {}", id);
                    return new ResourceNotFoundException("Group not found with ID: " + id);
                });
    }

    /**
     * Retrieves all groups.
     * @return List of GroupDTOs.
     */
    @Transactional(readOnly = true)
    public List<GroupDTO> getAllGroups() {
        log.debug("Fetching all groups.");
        List<GroupEntity> groups = groupRepository.findAll();
        log.info("Found {} groups.", groups.size());
        return groups.stream()
                .map(groupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a group by its UUID.
     * @param id The UUID of the group to delete.
     * @throws ResourceNotFoundException if group is not found.
     * @throws OperationNotAllowedException if the group has associated people or subgroups.
     */
    public void deleteGroup(UUID id) { // <<<--- UUID
        log.info("Attempting to delete group with ID: {}", id);
        // Check if group exists
        if (!groupRepository.existsById(id)) { // <<<--- existsById com UUID
            log.warn("Group deletion failed: Group not found with ID {}", id);
            throw new ResourceNotFoundException("Group not found with ID: " + id);
        }

        // Check if there are people associated with this group
        if (personRepository.existsByGroup_Id(id)) { // <<<--- existsByGroup_Id com UUID
            log.warn("Group deletion failed: Group ID {} has associated people.", id);
            throw new OperationNotAllowedException("Cannot delete group: Associated people exist.");
        }

        // Check if there are subgroups associated with this group
        if (subGroupRepository.existsByGroup_Id(id)) { // <<<--- existsByGroup_Id com UUID
            log.warn("Group deletion failed: Group ID {} has associated subgroups.", id);
            throw new OperationNotAllowedException("Cannot delete group: Associated subgroups exist. Delete subgroups first.");
        }

        groupRepository.deleteById(id); // <<<--- deleteById com UUID
        log.info("Successfully deleted group with ID: {}", id);
    }
}
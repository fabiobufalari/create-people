package com.bufalari.people.service;

import com.bufalari.people.convert.GroupConverter;
import com.bufalari.people.dto.GroupDTO;
import com.bufalari.people.entity.GroupEntity;
import com.bufalari.people.exception.OperationNotAllowedException; // New exception for delete checks
import com.bufalari.people.exception.ResourceAlreadyExistsException; // New exception for name check
import com.bufalari.people.exception.ResourceNotFoundException;
import com.bufalari.people.repository.GroupRepository;
import com.bufalari.people.repository.PersonRepository;
import com.bufalari.people.repository.SubGroupRepository; // Added SubGroupRepository for delete check
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service layer for managing Group entities.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final PersonRepository personRepository;
    private final SubGroupRepository subGroupRepository; // Inject SubGroupRepository
    private final GroupConverter groupConverter;

    /**
     * Creates a new group.
     * @param groupDTO DTO containing group data.
     * @return The created GroupDTO.
     * @throws ResourceAlreadyExistsException if group name already exists.
     */
    public GroupDTO createGroup(GroupDTO groupDTO) {
        log.debug("Attempting to create group with name: {}", groupDTO.getName());
        // Check for duplicate group name
        if (groupRepository.existsByName(groupDTO.getName())) {
            log.warn("Group creation failed: Name '{}' already exists.", groupDTO.getName());
            throw new ResourceAlreadyExistsException("Group with name '" + groupDTO.getName() + "' already exists.");
        }

        GroupEntity groupEntity = groupConverter.dtoToEntity(groupDTO);
        GroupEntity savedEntity = groupRepository.save(groupEntity);
        log.info("Successfully created group with ID: {}", savedEntity.getId());
        return groupConverter.entityToDTO(savedEntity);
    }

    /**
     * Updates an existing group.
     * @param id The ID of the group to update.
     * @param groupDTO DTO containing updated data.
     * @return The updated GroupDTO.
     * @throws ResourceNotFoundException if group is not found.
     * @throws ResourceAlreadyExistsException if changing name to an existing one.
     */
    public GroupDTO updateGroup(Long id, GroupDTO groupDTO) {
        log.debug("Attempting to update group with ID: {}", id);
        GroupEntity existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Group update failed: Group not found with ID {}", id);
                    return new ResourceNotFoundException("Group not found with ID: " + id);
                });

        // Check if the name is being changed and if the new name already exists
        if (!Objects.equals(existingGroup.getName(), groupDTO.getName())) {
            if (groupRepository.existsByName(groupDTO.getName())) {
                log.warn("Group update failed: Name '{}' already exists for another group.", groupDTO.getName());
                throw new ResourceAlreadyExistsException("Group with name '" + groupDTO.getName() + "' already exists.");
            }
            existingGroup.setName(groupDTO.getName()); // Update name
        }
        // Update type
        existingGroup.setType(groupDTO.getType());


        GroupEntity updatedEntity = groupRepository.save(existingGroup);
        log.info("Successfully updated group with ID: {}", id);
        return groupConverter.entityToDTO(updatedEntity);
    }

    /**
     * Retrieves a group by ID.
     * @param id The ID of the group.
     * @return The GroupDTO.
     * @throws ResourceNotFoundException if group is not found.
     */
    @Transactional(readOnly = true)
    public GroupDTO getGroupById(Long id) {
        log.debug("Fetching group by ID: {}", id);
        return groupRepository.findById(id)
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
        return groupRepository.findAll().stream()
                .map(groupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a group by ID.
     * @param id The ID of the group to delete.
     * @throws ResourceNotFoundException if group is not found.
     * @throws OperationNotAllowedException if the group has associated people or subgroups.
     */
    public void deleteGroup(Long id) {
        log.debug("Attempting to delete group with ID: {}", id);
        // Check if group exists
        if (!groupRepository.existsById(id)) {
             log.warn("Group deletion failed: Group not found with ID {}", id);
             throw new ResourceNotFoundException("Group not found with ID: " + id);
        }

        // Check if there are people associated with this group
        if (personRepository.existsByGroup_Id(id)) {
            log.warn("Group deletion failed: Group ID {} has associated people.", id);
            throw new OperationNotAllowedException("Cannot delete group: Associated people exist.");
        }

        // Check if there are subgroups associated with this group
        if (subGroupRepository.existsByGroup_Id(id)) {
             log.warn("Group deletion failed: Group ID {} has associated subgroups.", id);
             throw new OperationNotAllowedException("Cannot delete group: Associated subgroups exist. Delete subgroups first.");
        }


        groupRepository.deleteById(id);
        log.info("Successfully deleted group with ID: {}", id);
    }
}
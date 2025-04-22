package com.bufalari.people.service;

import com.bufalari.people.convert.SubGroupConverter;
import com.bufalari.people.dto.SubGroupDTO;
import com.bufalari.people.entity.GroupEntity;
import com.bufalari.people.entity.SubGroupEntity;
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
import java.util.stream.Collectors;

/**
 * Service layer for managing SubGroup entities.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubGroupService {

    private static final Logger log = LoggerFactory.getLogger(SubGroupService.class);

    private final SubGroupRepository subGroupRepository;
    private final GroupRepository groupRepository;
    private final PersonRepository personRepository;
    private final SubGroupConverter subGroupConverter;

    /**
     * Creates a new subgroup under a specified group.
     * @param subGroupDTO DTO containing subgroup data (including parent groupId).
     * @return The created SubGroupDTO.
     * @throws ResourceNotFoundException if the parent group is not found.
     * @throws ResourceAlreadyExistsException if subgroup name already exists within the parent group.
     */
    public SubGroupDTO createSubGroup(SubGroupDTO subGroupDTO) {
        log.debug("Attempting to create subgroup '{}' under group ID {}", subGroupDTO.getName(), subGroupDTO.getGroupId());
        // Find parent group
        GroupEntity parentGroup = groupRepository.findById(subGroupDTO.getGroupId())
                .orElseThrow(() -> {
                     log.warn("Subgroup creation failed: Parent Group not found with ID {}", subGroupDTO.getGroupId());
                     return new ResourceNotFoundException("Parent Group not found with ID: " + subGroupDTO.getGroupId());
                 });

        // Check if subgroup name already exists within this group
        if (subGroupRepository.findByNameAndGroup(subGroupDTO.getName(), parentGroup).isPresent()) {
             log.warn("Subgroup creation failed: Name '{}' already exists in group '{}'", subGroupDTO.getName(), parentGroup.getName());
             throw new ResourceAlreadyExistsException("Subgroup with name '" + subGroupDTO.getName() + "' already exists in group '" + parentGroup.getName() + "'.");
        }


        SubGroupEntity subGroupEntity = subGroupConverter.dtoToEntity(subGroupDTO);
        subGroupEntity.setGroup(parentGroup); // Set the parent group relationship

        SubGroupEntity savedEntity = subGroupRepository.save(subGroupEntity);
        log.info("Successfully created subgroup with ID: {} under group ID {}", savedEntity.getId(), parentGroup.getId());
        return subGroupConverter.entityToDTO(savedEntity);
    }

    /**
     * Updates an existing subgroup.
     * @param id The ID of the subgroup to update.
     * @param subGroupDTO DTO containing updated data (including potentially changed parent groupId).
     * @return The updated SubGroupDTO.
     * @throws ResourceNotFoundException if subgroup or new parent group is not found.
     * @throws ResourceAlreadyExistsException if name is changed and already exists in the target group.
     */
    public SubGroupDTO updateSubGroup(Long id, SubGroupDTO subGroupDTO) {
        log.debug("Attempting to update subgroup with ID: {}", id);
        SubGroupEntity existingSubGroup = subGroupRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subgroup update failed: Subgroup not found with ID {}", id);
                    return new ResourceNotFoundException("SubGroup not found with ID: " + id);
                });

        GroupEntity targetGroup;
        // Check if parent group is being changed
        if (!Objects.equals(existingSubGroup.getGroup().getId(), subGroupDTO.getGroupId())) {
            targetGroup = groupRepository.findById(subGroupDTO.getGroupId())
                    .orElseThrow(() -> {
                        log.warn("Subgroup update failed: New parent Group not found with ID {}", subGroupDTO.getGroupId());
                        return new ResourceNotFoundException("New parent Group not found with ID: " + subGroupDTO.getGroupId());
                    });
            existingSubGroup.setGroup(targetGroup); // Update parent group relationship
            log.debug("Changed parent group for subgroup {} to '{}'", id, targetGroup.getName());
        } else {
            targetGroup = existingSubGroup.getGroup(); // Use the existing group
        }

        // Check if name is being changed and if it exists in the target group
        if (!Objects.equals(existingSubGroup.getName(), subGroupDTO.getName())) {
             if (subGroupRepository.findByNameAndGroup(subGroupDTO.getName(), targetGroup).isPresent()) {
                 log.warn("Subgroup update failed: Name '{}' already exists in target group '{}'", subGroupDTO.getName(), targetGroup.getName());
                 throw new ResourceAlreadyExistsException("Subgroup with name '" + subGroupDTO.getName() + "' already exists in group '" + targetGroup.getName() + "'.");
             }
            existingSubGroup.setName(subGroupDTO.getName()); // Update name
        }

        SubGroupEntity updatedEntity = subGroupRepository.save(existingSubGroup);
        log.info("Successfully updated subgroup with ID: {}", id);
        return subGroupConverter.entityToDTO(updatedEntity);
    }

    /**
     * Retrieves a subgroup by ID.
     * @param id The ID of the subgroup.
     * @return The SubGroupDTO.
     * @throws ResourceNotFoundException if subgroup is not found.
     */
    @Transactional(readOnly = true)
    public SubGroupDTO getSubGroupById(Long id) {
        log.debug("Fetching subgroup by ID: {}", id);
        return subGroupRepository.findById(id)
                .map(subGroupConverter::entityToDTO)
                .orElseThrow(() -> {
                    log.warn("Subgroup retrieval failed: Subgroup not found with ID {}", id);
                    return new ResourceNotFoundException("SubGroup not found with ID: " + id);
                });
    }

    /**
     * Retrieves all subgroups.
     * @return List of SubGroupDTOs.
     */
    @Transactional(readOnly = true)
    public List<SubGroupDTO> getAllSubGroups() {
        log.debug("Fetching all subgroups.");
        return subGroupRepository.findAll().stream()
                .map(subGroupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all subgroups belonging to a specific parent group.
     * @param groupId The ID of the parent group.
     * @return List of SubGroupDTOs for that group.
     * @throws ResourceNotFoundException if the parent group is not found.
     */
    @Transactional(readOnly = true)
    public List<SubGroupDTO> getSubGroupsByGroup(Long groupId) {
        log.debug("Fetching subgroups for group ID: {}", groupId);
        // Ensure the group exists first
        if (!groupRepository.existsById(groupId)) {
             log.warn("Fetching subgroups failed: Parent Group not found with ID {}", groupId);
             throw new ResourceNotFoundException("Parent Group not found with ID: " + groupId);
        }
        return subGroupRepository.findByGroup_Id(groupId).stream()
                .map(subGroupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a subgroup by ID.
     * @param id The ID of the subgroup to delete.
     * @throws ResourceNotFoundException if subgroup is not found.
     * @throws OperationNotAllowedException if the subgroup has associated people.
     */
    public void deleteSubGroup(Long id) {
        log.debug("Attempting to delete subgroup with ID: {}", id);
         // Check if subgroup exists first
        if (!subGroupRepository.existsById(id)) {
            log.warn("Subgroup deletion failed: Subgroup not found with ID {}", id);
            throw new ResourceNotFoundException("SubGroup not found with ID: " + id);
        }


        // Check if there are people associated with this subgroup
        if (personRepository.existsBySubGroup_Id(id)) {
            log.warn("Subgroup deletion failed: Subgroup ID {} has associated people.", id);
            throw new OperationNotAllowedException("Cannot delete subgroup: Associated people exist.");
        }

        subGroupRepository.deleteById(id);
        log.info("Successfully deleted subgroup with ID: {}", id);
    }
}
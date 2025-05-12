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
import java.util.UUID; // <<<--- IMPORT UUID
import java.util.stream.Collectors;

/**
 * Service layer for managing SubGroup entities (using UUID).
 * Camada de serviço para gerenciar entidades SubGroup (usando UUID).
 */
@Service
@RequiredArgsConstructor // Injeta dependências finais
@Transactional // Transacionalidade padrão
public class SubGroupService {

    private static final Logger log = LoggerFactory.getLogger(SubGroupService.class);

    private final SubGroupRepository subGroupRepository;
    private final GroupRepository groupRepository;
    private final PersonRepository personRepository;
    private final SubGroupConverter subGroupConverter;

    /**
     * Creates a new subgroup under a specified group.
     * @param subGroupDTO DTO containing subgroup data (including parent groupId as UUID).
     * @return The created SubGroupDTO with generated UUID.
     * @throws ResourceNotFoundException if the parent group is not found.
     * @throws ResourceAlreadyExistsException if subgroup name already exists within the parent group.
     */
    public SubGroupDTO createSubGroup(SubGroupDTO subGroupDTO) {
        log.info("Attempting to create subgroup '{}' under group ID {}", subGroupDTO.getName(), subGroupDTO.getGroupId());
        if (subGroupDTO.getId() != null) {
            log.warn("ID provided for subgroup creation will be ignored.");
            subGroupDTO.setId(null);
        }
        // Find parent group by UUID
        GroupEntity parentGroup = groupRepository.findById(subGroupDTO.getGroupId()) // <<<--- Find by UUID
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
        subGroupEntity.setGroup(parentGroup); // Set relationship

        SubGroupEntity savedEntity = subGroupRepository.save(subGroupEntity);
        log.info("Successfully created subgroup '{}' with ID: {} under group ID {}", savedEntity.getName(), savedEntity.getId(), parentGroup.getId());
        return subGroupConverter.entityToDTO(savedEntity);
    }

    /**
     * Updates an existing subgroup.
     * @param id The UUID of the subgroup to update.
     * @param subGroupDTO DTO containing updated data (including potentially changed parent groupId as UUID).
     * @return The updated SubGroupDTO.
     * @throws ResourceNotFoundException if subgroup or new parent group is not found.
     * @throws ResourceAlreadyExistsException if name is changed and already exists in the target group.
     */
    public SubGroupDTO updateSubGroup(UUID id, SubGroupDTO subGroupDTO) { // <<<--- UUID
        log.info("Attempting to update subgroup with ID: {}", id);
        SubGroupEntity existingSubGroup = subGroupRepository.findById(id) // <<<--- Find by UUID
                .orElseThrow(() -> {
                    log.warn("Subgroup update failed: Subgroup not found with ID {}", id);
                    return new ResourceNotFoundException("SubGroup not found with ID: " + id);
                });

        GroupEntity targetGroup;
        // Check if parent group is being changed
        if (!Objects.equals(existingSubGroup.getGroup().getId(), subGroupDTO.getGroupId())) {
            log.debug("Parent group change detected for subgroup ID {}", id);
            targetGroup = groupRepository.findById(subGroupDTO.getGroupId()) // <<<--- Find by UUID
                    .orElseThrow(() -> {
                        log.warn("Subgroup update failed: New parent Group not found with ID {}", subGroupDTO.getGroupId());
                        return new ResourceNotFoundException("New parent Group not found with ID: " + subGroupDTO.getGroupId());
                    });
            existingSubGroup.setGroup(targetGroup);
            log.debug("Changed parent group for subgroup {} to '{}' (ID: {})", id, targetGroup.getName(), targetGroup.getId());
        } else {
            targetGroup = existingSubGroup.getGroup(); // Keep existing group
        }

        // Check if name is being changed and if it exists in the target group
        if (!Objects.equals(existingSubGroup.getName(), subGroupDTO.getName())) {
            log.debug("Subgroup name change detected for ID {}: '{}' -> '{}'", id, existingSubGroup.getName(), subGroupDTO.getName());
            if (subGroupRepository.findByNameAndGroup(subGroupDTO.getName(), targetGroup).isPresent()) {
                log.warn("Subgroup update failed: Name '{}' already exists in target group '{}'", subGroupDTO.getName(), targetGroup.getName());
                throw new ResourceAlreadyExistsException("Subgroup with name '" + subGroupDTO.getName() + "' already exists in group '" + targetGroup.getName() + "'.");
            }
            existingSubGroup.setName(subGroupDTO.getName());
        }

        SubGroupEntity updatedEntity = subGroupRepository.save(existingSubGroup);
        log.info("Successfully updated subgroup with ID: {}", id);
        return subGroupConverter.entityToDTO(updatedEntity);
    }

    /**
     * Retrieves a subgroup by its UUID.
     * @param id The UUID of the subgroup.
     * @return The SubGroupDTO.
     * @throws ResourceNotFoundException if subgroup is not found.
     */
    @Transactional(readOnly = true)
    public SubGroupDTO getSubGroupById(UUID id) { // <<<--- UUID
        log.debug("Fetching subgroup by ID: {}", id);
        return subGroupRepository.findById(id) // <<<--- Find by UUID
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
        List<SubGroupEntity> subGroups = subGroupRepository.findAll();
        log.info("Found {} subgroups.", subGroups.size());
        return subGroups.stream()
                .map(subGroupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all subgroups belonging to a specific parent group (by UUID).
     * @param groupId The UUID of the parent group.
     * @return List of SubGroupDTOs for that group.
     * @throws ResourceNotFoundException if the parent group is not found.
     */
    @Transactional(readOnly = true)
    public List<SubGroupDTO> getSubGroupsByGroup(UUID groupId) { // <<<--- UUID
        log.debug("Fetching subgroups for group ID: {}", groupId);
        // Ensure the group exists first
        if (!groupRepository.existsById(groupId)) { // <<<--- existsById com UUID
            log.warn("Fetching subgroups failed: Parent Group not found with ID {}", groupId);
            throw new ResourceNotFoundException("Parent Group not found with ID: " + groupId);
        }
        List<SubGroupEntity> subGroups = subGroupRepository.findByGroup_Id(groupId); // <<<--- findByGroup_Id com UUID
        log.info("Found {} subgroups for group ID {}", subGroups.size(), groupId);
        return subGroups.stream()
                .map(subGroupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a subgroup by its UUID.
     * @param id The UUID of the subgroup to delete.
     * @throws ResourceNotFoundException if subgroup is not found.
     * @throws OperationNotAllowedException if the subgroup has associated people.
     */
    public void deleteSubGroup(UUID id) { // <<<--- UUID
        log.info("Attempting to delete subgroup with ID: {}", id);
        // Check if subgroup exists first
        if (!subGroupRepository.existsById(id)) { // <<<--- existsById com UUID
            log.warn("Subgroup deletion failed: Subgroup not found with ID {}", id);
            throw new ResourceNotFoundException("SubGroup not found with ID: " + id);
        }

        // Check if there are people associated with this subgroup
        if (personRepository.existsBySubGroup_Id(id)) { // <<<--- existsBySubGroup_Id com UUID
            log.warn("Subgroup deletion failed: Subgroup ID {} has associated people.", id);
            throw new OperationNotAllowedException("Cannot delete subgroup: Associated people exist.");
        }

        subGroupRepository.deleteById(id); // <<<--- deleteById com UUID
        log.info("Successfully deleted subgroup with ID: {}", id);
    }
}
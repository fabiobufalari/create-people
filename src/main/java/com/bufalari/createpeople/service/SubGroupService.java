package com.bufalari.createpeople.service;


import com.bufalari.createpeople.convert.SubGroupConverter;
import com.bufalari.createpeople.dto.SubGroupDTO;
import com.bufalari.createpeople.entity.GroupEntity;
import com.bufalari.createpeople.entity.SubGroupEntity;
import com.bufalari.createpeople.exception.PersonAlreadyExistsException;
import com.bufalari.createpeople.exception.ResourceNotFoundException;
import com.bufalari.createpeople.repository.GroupRepository;
import com.bufalari.createpeople.repository.PersonRepository;
import com.bufalari.createpeople.repository.SubGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para operações relacionadas a SubGroup.
 * Service for operations related to SubGroup.
 */
@Service
@Transactional
public class SubGroupService {

    private final SubGroupRepository subGroupRepository;
    private final GroupRepository groupRepository;
    private final PersonRepository personRepository;
    private final SubGroupConverter subGroupConverter;

    public SubGroupService(SubGroupRepository subGroupRepository,
                           GroupRepository groupRepository,
                           PersonRepository personRepository,
                           SubGroupConverter subGroupConverter) {
        this.subGroupRepository = subGroupRepository;
        this.groupRepository = groupRepository;
        this.personRepository = personRepository;
        this.subGroupConverter = subGroupConverter;
    }

    public SubGroupDTO create(SubGroupDTO dto) {
        GroupEntity group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        SubGroupEntity subGroup = subGroupConverter.dtoToEntity(dto);
        subGroup.setGroup(group);
        SubGroupEntity saved = subGroupRepository.save(subGroup);
        return subGroupConverter.entityToDTO(saved);
    }

    public SubGroupDTO update(Long id, SubGroupDTO dto) {
        SubGroupEntity subGroup = subGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubGroup not found"));
        GroupEntity group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        subGroup.setName(dto.getName());
        subGroup.setGroup(group);
        SubGroupEntity updated = subGroupRepository.save(subGroup);
        return subGroupConverter.entityToDTO(updated);
    }

    public SubGroupDTO getById(Long id) {
        SubGroupEntity subGroup = subGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubGroup not found"));
        return subGroupConverter.entityToDTO(subGroup);
    }

    public List<SubGroupDTO> getAll() {
        return subGroupRepository.findAll().stream()
                .map(subGroupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<SubGroupDTO> getByGroup(Long groupId) {
        // Certifica que o grupo existe antes de buscar subgrupos
        // Ensure the group exists before fetching subgroups
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return subGroupRepository.findByGroup_Id(groupId).stream()
                .map(subGroupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SubGroupEntity subGroup = subGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubGroup not found"));
        // Verifica se há pessoas associadas a este subgrupo antes de deletar
        // Check if there are people associated with this subgroup before deleting
        if (personRepository.existsBySubGroup_Id(id)) {
            throw new PersonAlreadyExistsException("Cannot delete subgroup with associated people");
        }
        subGroupRepository.delete(subGroup);
    }
}

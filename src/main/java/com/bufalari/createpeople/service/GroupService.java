package com.bufalari.createpeople.service;


import com.bufalari.createpeople.convert.GroupConverter;
import com.bufalari.createpeople.dto.GroupDTO;
import com.bufalari.createpeople.entity.GroupEntity;
import com.bufalari.createpeople.exception.PersonAlreadyExistsException;
import com.bufalari.createpeople.exception.ResourceNotFoundException;
import com.bufalari.createpeople.repository.GroupRepository;
import com.bufalari.createpeople.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço para operações relacionadas a Group.
 * Service for operations related to Group.
 */
@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final PersonRepository personRepository;
    private final GroupConverter groupConverter;

    public GroupService(GroupRepository groupRepository,
                        PersonRepository personRepository,
                        GroupConverter groupConverter) {
        this.groupRepository = groupRepository;
        this.personRepository = personRepository;
        this.groupConverter = groupConverter;
    }

    public GroupDTO create(GroupDTO dto) {
        // Verifica duplicação de nome do grupo (nome de grupo deve ser único)
        // Check for duplicate group name (group name should be unique)
        // if (groupRepository.existsByName(dto.getName())) { throw new PersonAlreadyExistsException("Group with this name already exists"); }
        GroupEntity group = groupConverter.dtoToEntity(dto);
        GroupEntity saved = groupRepository.save(group);
        return groupConverter.entityToDTO(saved);
    }

    public GroupDTO update(Long id, GroupDTO dto) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        // Verifica se o nome está sendo alterado para um nome já existente
        // Check if the name is being changed to an already existing name
        // if (!group.getName().equals(dto.getName()) && groupRepository.existsByName(dto.getName())) { ... }
        group.setName(dto.getName());
        GroupEntity updated = groupRepository.save(group);
        return groupConverter.entityToDTO(updated);
    }

    public GroupDTO getById(Long id) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return groupConverter.entityToDTO(group);
    }

    public List<GroupDTO> getAll() {
        return groupRepository.findAll().stream()
                .map(groupConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        // Verifica se há pessoas associadas a este grupo antes de deletar
        // Check if there are people associated with this group before deleting
        if (personRepository.existsByGroup_Id(id)) {
            throw new PersonAlreadyExistsException("Cannot delete group with associated people");
        }
        groupRepository.delete(group);
    }
}

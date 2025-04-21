package com.bufalari.createpeople.convert;


import com.bufalari.createpeople.dto.GroupDTO;
import com.bufalari.createpeople.entity.GroupEntity;
import org.springframework.stereotype.Component;

/**
 * Conversor entre GroupEntity e GroupDTO.
 * Converter between GroupEntity and GroupDTO.
 */
@Component
public class GroupConverter {

    public GroupEntity dtoToEntity(GroupDTO dto) {
        // Converte GroupDTO para GroupEntity
        // Converts GroupDTO to GroupEntity
        return GroupEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    public GroupDTO entityToDTO(GroupEntity entity) {
        // Converte GroupEntity para GroupDTO
        // Converts GroupEntity to GroupDTO
        GroupDTO dto = new GroupDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}

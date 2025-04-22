package com.bufalari.people.convert;

import com.bufalari.people.dto.GroupDTO;
import com.bufalari.people.entity.GroupEntity;
import org.springframework.stereotype.Component;

/**
 * Converts between GroupEntity and GroupDTO.
 */
@Component
public class GroupConverter {

    public GroupEntity dtoToEntity(GroupDTO dto) {
        if (dto == null) {
            return null;
        }
        return GroupEntity.builder()
                .id(dto.getId()) // Keep ID for updates
                .name(dto.getName())
                .type(dto.getType()) // Include type
                .build();
    }

    public GroupDTO entityToDTO(GroupEntity entity) {
        if (entity == null) {
            return null;
        }
        // Use constructor for simpler DTO creation if available
        return new GroupDTO(
                entity.getId(),
                entity.getName(),
                entity.getType() // Include type
        );
    }
}
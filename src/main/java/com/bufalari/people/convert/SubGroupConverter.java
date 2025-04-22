package com.bufalari.people.convert;

import com.bufalari.people.dto.SubGroupDTO;
import com.bufalari.people.entity.SubGroupEntity;
import org.springframework.stereotype.Component;

/**
 * Converts between SubGroupEntity and SubGroupDTO.
 */
@Component
public class SubGroupConverter {

    /**
     * Converts SubGroupDTO to SubGroupEntity.
     * The parent Group relationship is set in the service layer.
     */
    public SubGroupEntity dtoToEntity(SubGroupDTO dto) {
         if (dto == null) {
             return null;
         }
        return SubGroupEntity.builder()
                .id(dto.getId()) // Keep ID for updates
                .name(dto.getName())
                // Group is set in the service
                .build();
    }

    /**
     * Converts SubGroupEntity to SubGroupDTO.
     * Includes parent group ID and name.
     */
    public SubGroupDTO entityToDTO(SubGroupEntity entity) {
        if (entity == null) {
            return null;
        }
        SubGroupDTO dto = new SubGroupDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getGroup() != null) {
            dto.setGroupId(entity.getGroup().getId());
            dto.setGroupName(entity.getGroup().getName());
        }
        return dto;
    }
}
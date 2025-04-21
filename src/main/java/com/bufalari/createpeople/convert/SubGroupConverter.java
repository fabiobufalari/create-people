package com.bufalari.createpeople.convert;


import com.bufalari.createpeople.dto.SubGroupDTO;
import com.bufalari.createpeople.entity.SubGroupEntity;
import org.springframework.stereotype.Component;

/**
 * Conversor entre SubGroupEntity e SubGroupDTO.
 * Converter between SubGroupEntity and SubGroupDTO.
 */
@Component
public class SubGroupConverter {

    public SubGroupEntity dtoToEntity(SubGroupDTO dto) {
        // Converte SubGroupDTO para SubGroupEntity (grupo associado separadamente)
        // Converts SubGroupDTO to SubGroupEntity (group is set separately)
        return SubGroupEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    public SubGroupDTO entityToDTO(SubGroupEntity entity) {
        // Converte SubGroupEntity para SubGroupDTO, incluindo nome do grupo
        // Converts SubGroupEntity to SubGroupDTO, including parent group name
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

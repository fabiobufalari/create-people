package com.bufalari.people.convert;

import com.bufalari.people.dto.SubGroupDTO;
import com.bufalari.people.entity.SubGroupEntity;
import org.springframework.stereotype.Component;
// Import UUID não necessário aqui

/**
 * Converts between SubGroupEntity (with UUID ID) and SubGroupDTO (with UUID ID).
 * Converte entre SubGroupEntity (com ID UUID) e SubGroupDTO (com ID UUID).
 */
@Component
public class SubGroupConverter {

    /**
     * Converts SubGroupDTO to SubGroupEntity.
     * The parent Group relationship must be set in the service layer.
     * Converte SubGroupDTO para SubGroupEntity.
     * A relação com o Grupo pai deve ser definida na camada de serviço.
     */
    public SubGroupEntity dtoToEntity(SubGroupDTO dto) {
        if (dto == null) {
            return null;
        }
        return SubGroupEntity.builder()
                .id(dto.getId()) // <<<--- UUID (Mantém para updates)
                .name(dto.getName())
                // O group é definido no serviço usando dto.getGroupId() (que agora é UUID)
                .build();
    }

    /**
     * Converts SubGroupEntity to SubGroupDTO.
     * Includes parent group UUID and name.
     * Converte SubGroupEntity para SubGroupDTO.
     * Inclui UUID e nome do grupo pai.
     */
    public SubGroupDTO entityToDTO(SubGroupEntity entity) {
        if (entity == null) {
            return null;
        }
        SubGroupDTO dto = new SubGroupDTO();
        dto.setId(entity.getId()); // <<<--- UUID
        dto.setName(entity.getName());
        // Preenche dados do grupo pai, se existir
        if (entity.getGroup() != null) {
            dto.setGroupId(entity.getGroup().getId()); // <<<--- UUID do grupo pai
            dto.setGroupName(entity.getGroup().getName());
        }
        return dto;
    }
}
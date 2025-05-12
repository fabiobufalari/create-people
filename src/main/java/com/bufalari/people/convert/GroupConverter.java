package com.bufalari.people.convert;

import com.bufalari.people.dto.GroupDTO;
import com.bufalari.people.entity.GroupEntity;
import org.springframework.stereotype.Component;
// Import UUID não necessário aqui

/**
 * Converts between GroupEntity (with UUID ID) and GroupDTO (with UUID ID).
 * Converte entre GroupEntity (com ID UUID) e GroupDTO (com ID UUID).
 */
@Component
public class GroupConverter {

    /**
     * Converts GroupDTO to GroupEntity.
     */
    public GroupEntity dtoToEntity(GroupDTO dto) {
        if (dto == null) {
            return null;
        }
        return GroupEntity.builder()
                .id(dto.getId()) // <<<--- UUID (Mantém para updates)
                .name(dto.getName())
                .type(dto.getType())
                // SubGroups não são mapeados aqui, gerenciados pela relação
                .build();
    }

    /**
     * Converts GroupEntity to GroupDTO.
     */
    public GroupDTO entityToDTO(GroupEntity entity) {
        if (entity == null) {
            return null;
        }
        // Usa construtor do DTO para simplicidade
        return new GroupDTO(
                entity.getId(), // <<<--- UUID
                entity.getName(),
                entity.getType()
        );
        // Alternativa com builder:
        // return GroupDTO.builder()
        //         .id(entity.getId())
        //         .name(entity.getName())
        //         .type(entity.getType())
        //         .build();
    }
}
package com.bufalari.service;

import com.bufalari.convert.ClientConverter;
import com.bufalari.dto.AlternativeContactDTO;
import com.bufalari.entity.AlternativeContactEntity;
import com.bufalari.entity.ClientEntity;
import com.bufalari.repository.AlternativeContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing alternative contact entities.
 */
@Service
public class AlternativeContactService {

    private static final Logger logger = LoggerFactory.getLogger(AlternativeContactService.class);

    private final AlternativeContactRepository alternativeContactRepository;
    private final ClientConverter clientConverter;

    /**
     * Constructor for AlternativeContactService.
     *
     * @param alternativeContactRepository The repository for accessing alternative contact data.
     * @param clientConverter                The converter for mapping between alternative contact entities and DTOs.
     */
    @Autowired
    public AlternativeContactService(AlternativeContactRepository alternativeContactRepository, ClientConverter clientConverter) {
        this.alternativeContactRepository = alternativeContactRepository;
        this.clientConverter = clientConverter;
    }

    /**
     * Saves a list of alternative contacts for a client.
     *
     * @param clientEntity The ClientEntity to which the alternative contacts belong.
     * @param contactDTOs  The list of AlternativeContactDTOs to save.
     * @param traceId      A unique identifier for tracking the request.
     */
    @Transactional
    public void saveAlternativeContacts(ClientEntity clientEntity, List<AlternativeContactDTO> contactDTOs, String traceId) {
        if (contactDTOs != null) {
            logger.debug("[TRACE-ID: {}] - Saving {} alternative contacts for client: {}", traceId, contactDTOs.size(), clientEntity.getId());
            for (AlternativeContactDTO contactDTO : contactDTOs) {
                AlternativeContactEntity contactEntity = new AlternativeContactEntity();
                contactEntity.setClient(clientEntity);
                contactEntity.setName(contactDTO.getName());
                contactEntity.setDDI(contactDTO.getDdI());
                contactEntity.setEmail(contactDTO.getEmail());
                contactEntity.setPhoneNumber(contactDTO.getPhoneNumber());
                contactEntity.setNotes(contactDTO.getNotes());
                alternativeContactRepository.save(contactEntity);
            }
        }
    }

    /**
     * Updates the alternative contacts for a client.
     *
     * @param existingClient The ClientEntity for which the alternative contacts are being updated.
     * @param contactDTOs  The updated list of AlternativeContactDTOs.
     * @param traceId      A unique identifier for tracking the request.
     */
    @Transactional
    public void updateAlternativeContacts(ClientEntity existingClient, List<AlternativeContactDTO> contactDTOs, String traceId) {
        if (contactDTOs != null) {
            List<AlternativeContactEntity> contactsToSave = new ArrayList<>();

            for (AlternativeContactDTO contactDTO : contactDTOs) {
                Optional<AlternativeContactEntity> existingContact = existingClient.getAlternativeContacts().stream()
                        .filter(c -> c.getId() != null && c.getId().equals(contactDTO.getId())) // Verifica pelo ID
                        .findFirst();

                if (existingContact.isPresent()) {
                    // Atualiza o contato existente
                    AlternativeContactEntity contactEntity = existingContact.get();
                    contactEntity.setName(contactDTO.getName());
                    contactEntity.setDDI(contactDTO.getDdI());
                    contactEntity.setPhoneNumber(contactDTO.getPhoneNumber());
                    contactEntity.setEmail(contactDTO.getEmail());
                    contactEntity.setNotes(contactDTO.getNotes());
                    contactsToSave.add(contactEntity);
                } else {
                    // Cria um novo contato
                    AlternativeContactEntity newContactEntity = new AlternativeContactEntity();
                    newContactEntity.setClient(existingClient);
                    newContactEntity.setName(contactDTO.getName());
                    newContactEntity.setDDI(contactDTO.getDdI());
                    newContactEntity.setPhoneNumber(contactDTO.getPhoneNumber());
                    newContactEntity.setEmail(contactDTO.getEmail());
                    newContactEntity.setNotes(contactDTO.getNotes());
                    contactsToSave.add(newContactEntity);
                }
            }

            alternativeContactRepository.saveAll(contactsToSave);
        }
    }
}
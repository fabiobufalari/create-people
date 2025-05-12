package com.bufalari.people.config;

import com.bufalari.people.entity.CompanyEntity; // Import correto
import com.bufalari.people.entity.GroupEntity;
import com.bufalari.people.entity.SubGroupEntity;
import com.bufalari.people.repository.CompanyRepository;
import com.bufalari.people.repository.GroupRepository;
import com.bufalari.people.repository.SubGroupRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID; // <<<--- IMPORT UUID

@Configuration
@RequiredArgsConstructor
@Profile("!test") // Não rodar durante testes unitários/integração
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final GroupRepository groupRepository;
    private final SubGroupRepository subGroupRepository;
    private final CompanyRepository companyRepository; // Adicionado repositório

    // Definir UUIDs fixos para referência (CUIDADO: usar apenas para dados iniciais controlados)
    private static final UUID DEFAULT_COMPANY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CLIENT_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID EMPLOYEE_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000200");
    private static final UUID SUPPLIER_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000300");
    // Adicione mais UUIDs fixos para subgrupos se necessário

    @PostConstruct
    @Transactional // Garante atomicidade
    public void loadInitialData() {
        log.info("Checking and loading initial data...");

        // Empresa Padrão (com UUID fixo)
        if (!companyRepository.existsById(DEFAULT_COMPANY_UUID)) {
            CompanyEntity company = CompanyEntity.builder()
                    .id(DEFAULT_COMPANY_UUID) // Define o ID manualmente
                    .name("Default Construction Co.")
                    .country("Canada")
                    .province("Nova Scotia")
                    .city("Halifax")
                    // Campos de auditoria serão preenchidos automaticamente
                    .build();
            companyRepository.save(company);
            log.info("Created default company: {} with ID {}", company.getName(), company.getId());
        } else {
            log.debug("Default company with ID {} already exists.", DEFAULT_COMPANY_UUID);
        }

        // Grupos Padrão (com UUIDs fixos)
        GroupEntity clientGroup = loadGroupIfNotExists("Clientes", "CLIENT", CLIENT_GROUP_UUID);
        GroupEntity employeeGroup = loadGroupIfNotExists("Funcionários", "EMPLOYEE", EMPLOYEE_GROUP_UUID);
        loadGroupIfNotExists("Fornecedores", "SUPPLIER", SUPPLIER_GROUP_UUID);

        // Subgrupos de Clientes
        if (clientGroup != null) {
            loadSubGroupIfNotExists("Cliente Pessoa Física", clientGroup);
            loadSubGroupIfNotExists("Cliente Pessoa Jurídica", clientGroup);
        } else {
            log.warn("Could not find 'Clientes' group (ID {}) to add subgroups.", CLIENT_GROUP_UUID);
        }

        // Subgrupos de Funcionários
        if (employeeGroup != null) {
            loadSubGroupIfNotExists("Engenheiro Civil", employeeGroup);
            loadSubGroupIfNotExists("Mestre de Obras", employeeGroup);
            loadSubGroupIfNotExists("Administrativo", employeeGroup);
        } else {
            log.warn("Could not find 'Funcionários' group (ID {}) to add subgroups.", EMPLOYEE_GROUP_UUID);
        }

        log.info("Initial data loading complete.");
    }

    private GroupEntity loadGroupIfNotExists(String name, String type, UUID fixedId) {
        if (!groupRepository.existsById(fixedId)) {
            // Verifica se o nome já existe (pode acontecer se o ID for diferente mas o nome igual)
            if (groupRepository.findByName(name).isPresent()) {
                log.warn("Group with name '{}' already exists but with a different ID. Skipping creation for fixed ID {}.", name, fixedId);
                return groupRepository.findByName(name).get(); // Retorna o existente pelo nome
            }
            GroupEntity group = GroupEntity.builder()
                    .id(fixedId) // Define o ID fixo
                    .name(name)
                    .type(type)
                    .build();
            group = groupRepository.save(group);
            log.info("Created group: {} with ID {}", name, fixedId);
            return group;
        } else {
            log.debug("Group '{}' with ID {} already exists.", name, fixedId);
            return groupRepository.findById(fixedId).orElse(null); // Retorna o existente pelo ID
        }
    }

    private void loadSubGroupIfNotExists(String name, GroupEntity parentGroup) {
        // Verifica se o subgrupo com este nome existe *dentro deste grupo específico*
        boolean exists = subGroupRepository.findByNameAndGroup(name, parentGroup).isPresent();
        if (!exists) {
            SubGroupEntity subGroup = SubGroupEntity.builder()
                    .name(name)
                    .group(parentGroup)
                    // ID será gerado automaticamente (não usamos fixo para subgrupos neste exemplo)
                    .build();
            subGroup = subGroupRepository.save(subGroup);
            log.info("Created subgroup '{}' (ID: {}) under group '{}'", name, subGroup.getId(), parentGroup.getName());
        } else {
            log.debug("Subgroup '{}' under group '{}' already exists.", name, parentGroup.getName());
        }
    }
}
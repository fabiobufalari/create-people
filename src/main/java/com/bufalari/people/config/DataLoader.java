package com.bufalari.people.config;

import com.bufalari.people.entity.CompanyEntity;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Profile("!test") // Não rodar durante testes se você tiver um application-test.yml que não precise disso
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final GroupRepository groupRepository;
    private final SubGroupRepository subGroupRepository;
    private final CompanyRepository companyRepository;

    private static final UUID DEFAULT_COMPANY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CLIENT_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID EMPLOYEE_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000200");
    private static final UUID SUPPLIER_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000300");

    @PostConstruct
    @Transactional
    public void loadInitialData() {
        log.info("Checking and loading initial data for create-people-service...");

        // Empresa Padrão
        CompanyEntity defaultCompany = companyRepository.findById(DEFAULT_COMPANY_UUID).orElseGet(() -> {
            CompanyEntity company = CompanyEntity.builder()
                    .id(DEFAULT_COMPANY_UUID)
                    .name("Default Construction Co.")
                    .businessIdentificationNumber("DEFAULT-BIN-001")
                    .mainActivity("General Construction Services") // <<<--- DEFINIR MAIN_ACTIVITY
                    .country("Canada")
                    .province("Nova Scotia")
                    .city("Halifax")
                    .foundationDate(LocalDate.of(2000, 1, 1))
                    .build();
            log.info("Creating default company: {} with ID {}", company.getName(), company.getId());
            return companyRepository.save(company);
        });
        log.debug("Default company '{}' (ID: {}) available.", defaultCompany.getName(), defaultCompany.getId());


        // Grupos Padrão
        GroupEntity clientGroup = loadGroupIfNotExists("Clientes", "CLIENT", CLIENT_GROUP_UUID);
        GroupEntity employeeGroup = loadGroupIfNotExists("Funcionários", "EMPLOYEE", EMPLOYEE_GROUP_UUID);
        loadGroupIfNotExists("Fornecedores", "SUPPLIER", SUPPLIER_GROUP_UUID);

        // Subgrupos de Clientes
        if (clientGroup != null) {
            loadSubGroupIfNotExists("Cliente Pessoa Física", clientGroup);
            loadSubGroupIfNotExists("Cliente Pessoa Jurídica", clientGroup);
        } else {
            log.warn("Could not find 'Clientes' group (intended ID {}) to add subgroups.", CLIENT_GROUP_UUID);
        }

        // Subgrupos de Funcionários
        if (employeeGroup != null) {
            loadSubGroupIfNotExists("Engenheiro Civil", employeeGroup);
            loadSubGroupIfNotExists("Mestre de Obras", employeeGroup);
            loadSubGroupIfNotExists("Administrativo", employeeGroup);
        } else {
            log.warn("Could not find 'Funcionários' group (intended ID {}) to add subgroups.", EMPLOYEE_GROUP_UUID);
        }

        log.info("Initial data loading for create-people-service complete.");
    }

    private GroupEntity loadGroupIfNotExists(String name, String type, UUID fixedId) {
        return groupRepository.findById(fixedId).orElseGet(() -> {
            Optional<GroupEntity> existingByName = groupRepository.findByName(name);
            if (existingByName.isPresent()) {
                log.warn("Group with name '{}' already exists with ID {}. Using existing.", name, existingByName.get().getId());
                return existingByName.get();
            }
            GroupEntity group = GroupEntity.builder()
                    .id(fixedId)
                    .name(name)
                    .type(type)
                    .build();
            log.info("Creating group: {} with ID {}", name, fixedId);
            return groupRepository.save(group);
        });
    }

    private void loadSubGroupIfNotExists(String name, GroupEntity parentGroup) {
        if (parentGroup == null) {
            log.warn("Cannot load subgroup '{}' because parent group is null.", name);
            return;
        }
        boolean exists = subGroupRepository.findByNameAndGroup(name, parentGroup).isPresent();
        if (!exists) {
            SubGroupEntity subGroup = SubGroupEntity.builder()
                    .name(name)
                    .group(parentGroup)
                    .build();
            subGroupRepository.save(subGroup);
            log.info("Created subgroup '{}' under group '{}'", name, parentGroup.getName());
        } else {
            log.debug("Subgroup '{}' under group '{}' already exists.", name, parentGroup.getName());
        }
    }
}
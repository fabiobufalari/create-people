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
import org.springframework.context.annotation.Profile; // Load only in specific profiles if needed
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Profile("!test") // Example: Avoid running loader during tests
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final GroupRepository groupRepository;
    private final SubGroupRepository subGroupRepository;
    private final CompanyRepository companyRepository; // Added repository

    @PostConstruct
    @Transactional // Ensure atomicity
    public void loadInitialData() {
        log.info("Checking and loading initial data...");

        // Default Company (ID 1)
        if (!companyRepository.existsById(1L)) {
            CompanyEntity company = CompanyEntity.builder()
                    .id(1L) // Manually set ID for predictable reference
                    .name("Default Construction Co.")
                    .country("Canada")
                    .province("Nova Scotia")
                    .city("Halifax")
                    .build();
            companyRepository.save(company);
            log.info("Created default company: {}", company.getName());
        } else {
             log.debug("Default company already exists.");
        }


        // Default Groups (Clientes, Funcionários, Fornecedores)
        loadGroupIfNotExists("Clientes", "CLIENT");
        loadGroupIfNotExists("Funcionários", "EMPLOYEE");
        loadGroupIfNotExists("Fornecedores", "SUPPLIER");

        // Example: Add Subgroup for Clientes
        GroupEntity clientGroup = groupRepository.findByName("Clientes").orElse(null);
        if (clientGroup != null) {
           loadSubGroupIfNotExists("Cliente Pessoa Física", clientGroup);
           loadSubGroupIfNotExists("Cliente Pessoa Jurídica", clientGroup);
        } else {
             log.warn("Could not find 'Clientes' group to add subgroups.");
        }

         // Example: Add Subgroup for Funcionários
        GroupEntity employeeGroup = groupRepository.findByName("Funcionários").orElse(null);
        if (employeeGroup != null) {
           loadSubGroupIfNotExists("Engenheiro Civil", employeeGroup);
           loadSubGroupIfNotExists("Mestre de Obras", employeeGroup);
           loadSubGroupIfNotExists("Administrativo", employeeGroup);
        } else {
             log.warn("Could not find 'Funcionários' group to add subgroups.");
        }


        log.info("Initial data loading complete.");
    }

    private void loadGroupIfNotExists(String name, String type) {
        if (groupRepository.findByName(name).isEmpty()) {
            GroupEntity group = GroupEntity.builder()
                .name(name)
                .type(type) // Assuming 'type' is a relevant classification
                .build();
            groupRepository.save(group);
            log.info("Created group: {}", name);
        } else {
             log.debug("Group '{}' already exists.", name);
        }
    }

     private void loadSubGroupIfNotExists(String name, GroupEntity parentGroup) {
         // Check if subgroup with this name exists *within this specific group*
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
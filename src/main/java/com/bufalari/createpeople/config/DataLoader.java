package com.bufalari.createpeople.config;

import com.bufalari.createpeople.entity.CompanyEntity;
import com.bufalari.createpeople.entity.GroupEntity;
import com.bufalari.createpeople.entity.SubGroupEntity;
import com.bufalari.createpeople.repository.CompanyRepository;
import com.bufalari.createpeople.repository.GroupRepository;
import com.bufalari.createpeople.repository.SubGroupRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final GroupRepository groupRepository;
    private final SubGroupRepository subGroupRepository;
    private final CompanyRepository companyRepository;

    @PostConstruct
    public void loadData() {

        // Empresa com ID 1
        if (!companyRepository.existsById(1L)) {
            CompanyEntity company = CompanyEntity.builder()
                    .id(1L)
                    .name("Empresa Padrão")
                    .country("Canada")
                    .province("Nova Scotia")
                    .city("Halifax")
                    .build();
            companyRepository.save(company);
        }

        // Grupo com ID 1
        if (!groupRepository.existsById(1L)) {
            GroupEntity group = GroupEntity.builder()
                    .id(1L)
                    .name("Clientes")
                    .build();
            groupRepository.save(group);

            // Subgrupo com ID 1
            SubGroupEntity subGroup = SubGroupEntity.builder()
                    .id(1L)
                    .name("Cliente Pessoa Física")
                    .group(group)
                    .build();
            subGroupRepository.save(subGroup);
        }
    }
}

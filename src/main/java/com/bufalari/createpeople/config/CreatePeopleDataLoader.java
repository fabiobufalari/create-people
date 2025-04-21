package com.bufalari.createpeople.config;

import com.bufalari.createpeople.entity.GroupEntity;
import com.bufalari.createpeople.repository.GroupRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreatePeopleDataLoader {

    private final GroupRepository groupRepository;

    public CreatePeopleDataLoader(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @PostConstruct
    @Transactional
    public void loadData() {
        if (groupRepository.count() == 0) {
            groupRepository.save(new GroupEntity(null, "Clientes", "CLIENT"));
            groupRepository.save(new GroupEntity(null, "Funcionários", "EMPLOYEE"));
            groupRepository.save(new GroupEntity(null, "Fornecedores", "SUPPLIER"));
        }
    }
}

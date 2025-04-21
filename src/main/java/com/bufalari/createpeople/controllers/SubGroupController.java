package com.bufalari.createpeople.controllers;

import com.bufalari.createpeople.dto.SubGroupDTO;
import com.bufalari.createpeople.service.SubGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subgroups")
public class SubGroupController {

    private final SubGroupService service;

    public SubGroupController(SubGroupService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SubGroupDTO> create(@Valid @RequestBody SubGroupDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubGroupDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<SubGroupDTO>> getAll(@RequestParam(value = "groupId", required = false) Long groupId) {
        if (groupId != null) {
            return ResponseEntity.ok(service.getByGroup(groupId));
        }
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubGroupDTO> update(@PathVariable Long id, @Valid @RequestBody SubGroupDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

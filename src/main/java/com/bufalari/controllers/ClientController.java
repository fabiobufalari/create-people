package com.bufalari.controllers;

import com.bufalari.dto.ClientDTO;
import com.bufalari.dto.ClientResponseDTO;
import com.bufalari.exception.ClientAlreadyExistsException;
import com.bufalari.exception.ClientNotFoundException;
import com.bufalari.exception.InvalidClientDataException;
import com.bufalari.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;


    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clientService.getClientById(id));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@RequestBody ClientDTO clientDTO) {
        try {
            ClientResponseDTO createdClient = clientService.createClient(clientDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
        } catch (ClientAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(null); // Returns a 400 Bad Request
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable Long id, @RequestBody @Valid ClientDTO clientDTO) {
        try {
            // Passa o id e o DTO para o serviço para atualizar o cliente
            ClientResponseDTO updatedClient = clientService.updateClient(id, clientDTO);

            // Retorna o cliente atualizado com o código 200 (OK)
            return ResponseEntity.ok(updatedClient);
        } catch (ClientNotFoundException e) {
            // Se o cliente não for encontrado, retorna 404 (Not Found)
            return ResponseEntity.notFound().build();
        } catch (InvalidClientDataException e) {
            // Se os dados do cliente forem inválidos, retorna 400 (Bad Request)
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            // Qualquer outro erro retorna 500 (Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PatchMapping("/{id}")
    public ResponseEntity<Void> softDeleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build();
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<Void> activateClient(@PathVariable Long id) {
        try {
            clientService.activateClient(id);
            return ResponseEntity.noContent().build();
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/search-by-email")
    public ResponseEntity<ClientResponseDTO> getClientByEmail(@RequestParam String email) {
        try {
            return ResponseEntity.ok(clientService.getClientByEmail(email));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/search-by-sin")
    public ResponseEntity<ClientResponseDTO> getClientBySinNumber(@RequestParam String sin) {
        try {
            return ResponseEntity.ok(clientService.getClientBySinNumber(sin));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientResponseDTO>> searchClientsByName(@RequestParam String name) {
        return ResponseEntity.ok(clientService.searchClientsByName(name));
    }
}

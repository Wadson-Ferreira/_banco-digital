package br.com.cdb.bancodigital.controller;

import br.com.cdb.bancodigital.dto.mapper.ClienteMapper;
import br.com.cdb.bancodigital.dto.request.ClienteCreateRequestDTO;
import br.com.cdb.bancodigital.dto.request.ClienteUpdateRequestDTO;
import br.com.cdb.bancodigital.dto.response.ClienteResponseDTO;
import br.com.cdb.bancodigital.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    public ClienteController(ClienteService clienteService, ClienteMapper clienteMapper) {
        this.clienteService = clienteService;
        this.clienteMapper = clienteMapper;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteCreateRequestDTO dto){
        ClienteResponseDTO responseDTO = clienteService.create(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}")
                .buildAndExpand(responseDTO.getId()).toUri();
        return ResponseEntity.created(uri).body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteMapper.toClienteResponseDTO(clienteService.findById(id)));
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<ClienteResponseDTO> findByCpf(@PathVariable String cpf){
        ClienteResponseDTO dto = clienteService.findByCpf(cpf);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ClienteUpdateRequestDTO dto) {
        return ResponseEntity.ok(clienteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
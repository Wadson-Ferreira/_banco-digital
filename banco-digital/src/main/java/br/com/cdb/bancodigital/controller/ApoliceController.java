package br.com.cdb.bancodigital.controller;

import br.com.cdb.bancodigital.dto.request.ContratarSeguroRequestDTO;
import br.com.cdb.bancodigital.dto.request.RenovarSeguroViagemRequestDTO;
import br.com.cdb.bancodigital.dto.response.ApoliceResponseDTO;
import br.com.cdb.bancodigital.service.ApoliceService;
import br.com.cdb.bancodigital.service.SeguroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/apolices")
public class ApoliceController {

    private final ApoliceService apoliceService;
    private final SeguroService seguroService;

    public ApoliceController(ApoliceService apoliceService, SeguroService seguroService) {
        this.apoliceService = apoliceService;
        this.seguroService = seguroService;
    }

    @PostMapping
    public ResponseEntity<ApoliceResponseDTO> contratarSeguro(@Valid @RequestBody ContratarSeguroRequestDTO dto) {
        return ResponseEntity.status(201).body(seguroService.contratarSeguro(dto));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ApoliceResponseDTO>> findApolicesPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(apoliceService.findApolicesByCliente(clienteId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarSeguro(@PathVariable Long id) {
        apoliceService.cancelarSeguro(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/renovar-viagem")
    public ResponseEntity<ApoliceResponseDTO> renovarSeguroViagem(@PathVariable Long id, @Valid @RequestBody RenovarSeguroViagemRequestDTO dto) {
        return ResponseEntity.ok(apoliceService.renovarSeguroViagem(id, dto));
    }
}